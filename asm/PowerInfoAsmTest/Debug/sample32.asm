include 'win32a.inc'
format PE GUI 4.0
entry start
section '.text' code readable executable
start:

lea edi,[Output]


push ebx esi edi ebp edi
;--- Initializing output data, counter and destination pointer ---
xor esi,esi
add edi,16
;--- Get path for output string and IOCTL file I/O ---
;--- Get handle for access DEVICE INFORMATION SET with list of batteries ---
; Parm#1 = Pointer to device class GUID
; Parm#2 = Pointer to enumerator name
; Parm#3 = Pointer to parent (window) handle
; Parm#4 = Control flags for return filtering
; Return = EAX = Return handle for DEVICE INFORM. SET
;---
push DIGCF_FLAGS 0 0        ; Parm#4, Parm#3, Parm#2
push GUID_DEVCLASS_BATTERY  ; Parm#1 
call [SetupDiGetClassDevsA]                                               ; #1
test eax,eax
jz BreakEnumeration
xchg ebx,eax                             ; EBX = Handle for DEVICE INFORM. SET
;--- Start cycle for maximum 10 batteries ---
CycleEnumeration:
;--- Enumerate device interf., get path for output string and IOCTL file ---
; Parm#1 = Handle for DEVICE INFORMATION SET
; Parm#2 = Pointer to Dev. Info enum. control
; Parm#3 = Pointer to device class GUID
; Parm#4 = Member index
; Parm#5 = Pointer to Device Interface Data
; Return = EAX = Result flag, boolean
;---
lea eax,[edi+OPB.DeviceInterface]
mov dword [eax+DeviceInterfaceData.cbSize],28  ; NOTE 32/64
push eax                                  ; Parm#5
push esi GUID_DEVCLASS_BATTERY 0 ebx      ; Parm#4, Parm#3, Parm#2, Parm#1
call [SetupDiEnumDeviceInterfaces]                                        ; #2
test eax,eax
jz BreakEnumeration
;--- This call for detect required buffer size ---
; Parm#1 = Handle for DEVICE INFORMATION SET
; Parm#2 = Pointer to Device Interface Data
; Parm#3 = Pointer to Dev. Int. Detail Data
; Parm#4 = Size of Dev. Int. Detail Data
; Parm#5 = Pointer to output dword: req. size
; Parm#6 = Pointer to output devinfo data
; Return = RAX = Result flag, boolean
;---
lea eax,[edi+OPB.cbRequired]
mov dword [eax],0
push 0 eax                          ; Parm#6, Parm#5
push 0 0                            ; Parm#4, Parm#3
lea eax,[edi+OPB.DeviceInterface]
push eax ebx                        ; Parm#2, Parm#1 
call [SetupDiGetDeviceInterfaceDetailA]                                   ; #3
test eax,eax
jnz BreakEnumeration  ; Expected error because size too small
;--- Get error for detect required buffer size ---
call [GetLastError]
cmp eax,BUFFER_TOO_SMALL
jne BreakEnumeration
mov ecx,[edi+OPB.cbRequired]
cmp ecx,1024-8
ja BreakEnumeration  ; Go exit with error if required size too big
;--- Repeat with adjusted size ---
xor eax,eax
push eax eax                        ; Parm#6, Parm#5   
push ecx                            ; Parm#4
lea eax,[edi+OPB.Path1]
push eax                            ; Parm#3
mov dword [eax+DeviceInterfaceDetailData.cbSize],5
lea eax,[edi+OPB.DeviceInterface]
push eax ebx                        ; Parm#2, Parm#1
call [SetupDiGetDeviceInterfaceDetailA]                                   ; #4
test eax,eax
jz BreakEnumeration
;--- Open device as file for IOCTL operations ---
; Parm#1 = Pointer to name string
; Parm#2 = File access mode
; Parm#3 = File sharing mode
; Parm#4 = Security attributes
; Parm#5 = File create disposition
; Parm#6 = File attributes
; Parm#7 = Template file handle
;---
lea eax,[edi+OPB.Path1.DevicePath]
push ftpl fatr fcdp fsec fshr facc eax   ; Parms# [7-1]
call [CreateFile]                                                         ; #5
test eax,eax
jz BreakEnumeration
xchg ebp,eax                                            ; EBP = Battery handle
;--- IOCTL DeviceIoControl usage notes for Battery Info ---
; Parm#1 = RCX = HANDLE: device handle returned by CreateFile function
; Parm#2 = RDX = REQUEST CODE: IOCTL_BATTERY_QUERY_INFORMATION
; Parm#3 = R8  = POINTER: pointer to input buffer
; Parm#4 = R9  = DWORD: size of input buffer
; Parm#5 = POINTER: pointer to output buffer
; Parm#6 = DWORD: size of output buffer
; Parm#7 = POINTER: pointer to dword: variable output return size
; Parm#8 = POINTER: pointer to OVERLAPPED structure for asynchronous
; Return = RAX = Status
;--- Get BATTERY TAG and store for return to caller ---
lea eax,[edi+OPB.OutRet]
mov dword [eax],0
push 0 eax                               ; Parms# [8-7]
lea eax,[edi+OPB.InBuf.BatteryTag]
push 4 eax                               ; Parms# [6-5]
lea eax,[edi+OPB.ShortInBuf]
push 4 eax IOCTL_BATTERY_QUERY_TAG ebp   ; Parms# [4-1]
call [DeviceIoControl]                                                    ; #6
test eax,eax
jz BreakTag
cmp dword [edi+OPB.OutRet],0 
je BreakTag
;--- Begin big conditional block ---
;--- Battery model string ---
lea ecx,[edi+OPB.StatusBatteryModel]
mov edx,128
mov eax,BatteryDeviceName
call IoctlRequest                                                         ; #7
;--- Battery vendor string ---
lea ecx,[edi+OPB.StatusBatteryVendor]
mov edx,128
mov eax,BatteryManufactureName
call IoctlRequest                                                         ; #8
;--- Battery manufacture date as number and as string ---
lea ecx,[edi+OPB.StatusBatteryDate]
mov edx,16
mov eax,BatteryManufactureDate
call IoctlRequest                                                         ; #9
;--- Battery serial number string ---
lea ecx,[edi+OPB.StatusBatterySnum]
mov edx,128
mov eax,BatterySerialNumber
call IoctlRequest                                                         ; #10
;--- Battery unique id string ---
lea ecx,[edi+OPB.StatusBatteryUid]
mov edx,128
mov eax,BatteryUniqueId
call IoctlRequest                                                         ; #11
;--- Battery chemistry, as part of battery info ---
lea ecx,[edi+OPB.StatusBatteryInfo]
mov edx,64
mov eax,BatteryInformation
call IoctlRequest                                                         ; #12
;--- Battery temperature ---
lea ecx,[edi+OPB.StatusBatteryTemp]
mov edx,16
mov eax,BatteryTemperature
call IoctlRequest                                                         ; #13
;--- Battery status read, for next 4 parameters ---
push 0                                             ; Parm#8
lea eax,[edi+OPB.StatusBatteryStatus+08]
mov dword [eax+0],0
mov dword [eax+4],0
push eax                                           ; Parm#7
lea eax,[edi+OPB.StatusBatteryStatus+16]
push 16 eax 20                                     ; Parms# [6-4]
lea ecx,[edi+OPB.WaitStatus]
mov eax,[edi+OPB.InBuf.BatteryTag]
mov [ecx+BATTERY_WAIT_STATUS.BatteryTag],eax
xor eax,eax
mov [ecx+BATTERY_WAIT_STATUS.Timeout],eax
mov [ecx+BATTERY_WAIT_STATUS.PowerState],eax
mov [ecx+BATTERY_WAIT_STATUS.LowCapacity],eax
mov [ecx+BATTERY_WAIT_STATUS.HighCapacity],eax
push ecx IOCTL_BATTERY_QUERY_STATUS ebp            ; Parms# [3-1] 
call [DeviceIoControl]                                                    ; #14
mov dword [edi+OPB.StatusBatteryStatus+00],eax
mov dword [edi+OPB.StatusBatteryStatus+04],0 
;--- End of big conditional block ---
BreakTag:
;--- Cycle for maximum 10 batteries ---
add edi,2048
inc esi
cmp esi,10
jb CycleEnumeration
;--- Exit point for errors and normal cycle termination ---
BreakEnumeration:
pop edi
mov dword [edi+0],esi
mov dword [edi+4],0
pop ebp edi esi ebx

;---------- Exit --------------------------------------------------------------;

push 0
call [ExitProcess]            ; Exit from application

;---------- Subroutine --------------------------------------------------------;

;-----------------------------------------------------;
; INPUT:   EAX = IOCTL Request                        ;
;          EDI = Base (statical) OPB Pointer          ;
;          RBP = Battery handle                       ; 
;          ECX = Current (dynamical) OPB Pointer      ;
;          EDX = Output data size                     ;
; OUTPUT:  OPB updated at [R14]                       ;
;-----------------------------------------------------;
IoctlRequest:
push ecx ebx
lea ebx,[ecx+08]
mov dword [ebx+00],0
mov dword [ebx+04],0
push 0 ebx                                         ; Parms# [8-7]
lea ebx,[ecx+16]
push edx ebx                                       ; Parms# [6-5]
lea ebx,[edi+OPB.InBuf]
mov [ebx+BATTERY_QUERY_INFORMATION.InformationLevel],eax
push 12 ebx IOCTL_BATTERY_QUERY_INFORMATION ebp    ; Parms# [4-1]
call [DeviceIoControl]
pop ebx ecx
mov dword [ecx+00],eax
mov dword [ecx+04],0 
ret



section '.data' data readable writeable

;--- GUID = {72631E54-78A4-11D0-BCF7-00AA00B7B32A} ---
GUID_DEVCLASS_BATTERY:
DD 072631E54h
DW 078A4h, 011D0h
DB 0BCh, 0F7h, 000h, 0AAh, 000h, 0B7h, 0B3h, 02Ah
;--- IOCTL base constants ---
DIGCF_FLAGS = 00000012h     ; PRESENT=02H, DEVICEINTERFACE=10H
BUFFER_TOO_SMALL = 122      ; Error code for buffer re-allocation
;--- Device IO control constants ---
IOCTL_BATTERY_QUERY_TAG         = 00294040h
IOCTL_BATTERY_QUERY_INFORMATION = 00294044h
IOCTL_BATTERY_QUERY_STATUS      = 0029404Ch
;--- IOCTL requests codes for battery ---
BatteryInformation = 0
BatteryDeviceName = 4
BatteryManufactureName = 6
BatteryManufactureDate = 5
BatterySerialNumber = 8
BatteryUniqueId = 7
BatteryTemperature = 2
;--- File IO constants ---
facc  = GENERIC_READ OR GENERIC_WRITE
fshr = FILE_SHARE_READ OR FILE_SHARE_WRITE
fsec = 0
fcdp = OPEN_EXISTING
fatr = FILE_ATTRIBUTE_NORMAL
ftpl = 0
;--- IOCTL common definitions ---
struct DeviceInterfaceData
cbSize     dd ?             ; Struct. size, caller must set
classguid  db 16 dup (?)    ; Interface class GUID
flags      dd ?             ; Interface flags
rsvd       dq ?             ; Reserved (note about 32/64 difference for size)
ends
;---
struct DeviceInfoData
cbSize     dd ?             ; Struct. size, caller must set
classguid  db 16 dup (?)    ; Interface class GUID
devinst    dd ?             ; Handle to device instance (devnode)
rsvd       dq ?             ; Reserved (note about 32/64 difference for size)
ends
;---
struct DeviceInterfaceDetailData
cbSize     dd ?             ; Struct. size, caller must set
DevicePath db 1024 dup (?)  ; Path for use file operations
ends
;--- IOCTL definitions for batteries ---
;--- IOCTL BATTERY INFORMATION request, output buffer formats ---
struct BATTERY_INFORMATION
Capabilities         dd ?    
Technology:          db ?
Reserved             db 3 dup (?)
Chemistry            dw 4 dup (?) ; UNICODE
DesignedCapacity     dd ?
FullChargedCapacity  dd ?
DefaultAlert1        dd ?
DefaultAlert2        dd ?
CriticalBias         dd ?
CycleCount           dd ?
ends
;--- IOCTL BATTERY INFORMATION request input buffer format ---
struct BATTERY_QUERY_INFORMATION
BatteryTag           dd ?
InformationLevel     dd ?    ; type is BATTERY_QUERY_INFORMATION_LEVEL
AtRate               dd ?
ends
;--- IOCTL BATTERY STATUS request input buffer format ---
; input buffer for this IOCTL request 
struct BATTERY_WAIT_STATUS
BatteryTag           dd ?    ; tag for battery select
Timeout              dd ?    ; timeouts handling option
PowerState           dd ?    ; select state, for output data association
LowCapacity          dd ?
HighCapacity         dd ?
ends
;--- IOCTL BATTERY STATUS request output buffer format ---
; output buffer for this IOCTL request
struct BATTERY_STATUS
PowerState           dd ?
Capacity             dd ?
Voltage              dd ?
Rate                 dd ?
ends
;--- Output Parameters Block ---
struct OPB
;--- Common data --- 16 bytes --- 2 qwords --- 
ItemsCount           dq ?
Reserved1            dq ?
;--- Start per-battery data --- 1104 bytes --- 138 qwords ---  
cbRequired           dd ?
OutRet               dd ?
ShortInBuf           dd ?
DeviceInterface      DeviceInterfaceData        ; 32 bytes
Path1                DeviceInterfaceDetailData  ; 1028 bytes
InBuf                BATTERY_QUERY_INFORMATION  ; 12 bytes
WaitStatus           BATTERY_WAIT_STATUS        ; 20 bytes
;--- Battery Model --- 128 bytes --- 16 qwords --- 
StatusBatteryModel   dq ?
SizeBatteryModel     dq ?
OutBufBatteryModel   db 112 dup (?) 
;--- Battery Vendor --- 128 bytes --- 16 qwords ---
StatusBatteryVendor  dq ?
SizeBatteryVendor    dq ?
OutBufBatteryVendor  db 112 dup (?) 
;--- Battery Date --- 32 bytes --- 4 qwords ---
StatusBatteryDate    dq ?
SizeBatteryDate      dq ?
OutBufBatteryDate    db 16 dup (?) 
;--- Battery Serial Number --- 128 bytes --- 16 qwords ---
StatusBatterySnum    dq ?
SizeBatterySnum      dq ?
OutBufBatterySnum    db 112 dup (?) 
;--- Battery Unique ID --- 128 bytes --- 16 qwords ---
StatusBatteryUid     dq ?
SizeBatteryUid       dq ?
OutBufBatteryUid     db 112 dup (?) 
;--- Battery Info --- 64 bytes --- 8 qwords ---
StatusBatteryInfo    dq ?
SizeBatteryInfo      dq ?
OutBufBatteryInfo    db 64 dup (?) 
;--- Battery Temperature --- 32 bytes --- 4 qwords ---
StatusBatteryTemp    dq ?
SizeBatteryTemp      dq ?
OutBufBatteryTemp    db 16 dup (?) 
;--- Battery Status --- 32 bytes --- 4 qwords ---
StatusBatteryStatus  dq ?
SizeBatteryStatus    dq ?
OutBufBatteryStatus  db 16 dup (?) 
;--- End ---
ends

;--- Buffer ---
Output:
OPB

section '.idata' import data readable writeable
library kernel32,'KERNEL32.DLL', setupapi, 'SETUPAPI.DLL'
include 'api\kernel32.inc'
import setupapi, \
SetupDiGetClassDevsA, 'SetupDiGetClassDevsA', \
SetupDiEnumDeviceInterfaces, 'SetupDiEnumDeviceInterfaces', \
SetupDiGetDeviceInterfaceDetailA, 'SetupDiGetDeviceInterfaceDetailA'




