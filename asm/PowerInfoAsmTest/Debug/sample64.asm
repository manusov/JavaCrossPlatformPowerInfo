include 'win64a.inc'
format PE64 GUI 5.0
entry start

section '.text' code readable executable
start:
sub rsp,8*5                   ; Create and align stack frame


lea rdi,[Output]


push rbx rsi rdi rbp r12 r13 r14 r15
mov rbp,rsp
and rsp,0FFFFFFFFFFFFFFF0h
sub rsp,32
;--- Initializing output data, counter and destination pointer ---
xor esi,esi
mov r12,rdi
add rdi,16
;--- Get path for output string and IOCTL file I/O ---
;--- Get handle for access DEVICE INFORMATION SET with list of batteries ---
; Parm#1 = RCX = Pointer to device class GUID
; Parm#2 = RDX = Pointer to enumerator name
; Parm#3 = R8  = Pointer to parent (window) handle
; Parm#4 = R9  = Control flags for return filtering
; Return = RAX = Return handle for DEVICE INFORM. SET
;---
lea rcx,[GUID_DEVCLASS_BATTERY]
xor edx,edx
xor r8d,r8d
mov r9d,DIGCF_FLAGS
call [SetupDiGetClassDevsA]                                               ; #1
test rax,rax
jz BreakEnumeration
xchg rbx,rax                             ; RBX = Handle for DEVICE INFORM. SET
;--- Start cycle for maximum 10 batteries ---
CycleEnumeration:
;--- Enumerate device interf., get path for output string and IOCTL file ---
; Parm#1 = RCX = Handle for DEVICE INFORMATION SET
; Parm#2 = RDX = Pointer to Dev. Info enum. control
; Parm#3 = R8  = Pointer to device class GUID
; Parm#4 = R9  = Member index
; Parm#5 = Pointer to Device Interface Data
; Return = RAX = Result flag, boolean
;---
mov rcx,rbx
xor edx,edx
lea r8,[GUID_DEVCLASS_BATTERY]
mov r9d,esi
lea rax,[rdi+OPB.DeviceInterface]
mov dword [rax+DeviceInterfaceData.cbSize],32
push rax rax
sub rsp,32
call [SetupDiEnumDeviceInterfaces]                                        ; #2
add rsp,32+16
test rax,rax
jz BreakEnumeration
;--- This call for detect required buffer size ---
; Parm#1 = RCX = Handle for DEVICE INFORMATION SET
; Parm#2 = RDX = Pointer to Device Interface Data
; Parm#3 = R8  = Pointer to Dev. Int. Detail Data
; Parm#4 = R9  = Size of Dev. Int. Detail Data
; Parm#5 = Pointer to output dword: req. size
; Parm#6 = Pointer to output devinfo data
; Return = RAX = Result flag, boolean
;---
mov rcx,rbx
lea rdx,[rdi+OPB.DeviceInterface]
xor r8d,r8d
xor r9d,r9d
push r8
lea rax,[rdi+OPB.cbRequired]
mov dword [rax],r9d
push rax
sub rsp,32
call [SetupDiGetDeviceInterfaceDetailA]                                   ; #3
add rsp,32+16
test rax,rax
jnz BreakEnumeration  ; Expected error because size too small
;--- Get error for detect required buffer size ---
call [GetLastError]
cmp rax,BUFFER_TOO_SMALL
jne BreakEnumeration
mov r9d,[rdi+OPB.cbRequired]
cmp r9d,1024-8
ja BreakEnumeration  ; Go exit with error if required size too big
;--- Repeat with adjusted size ---
mov rcx,rbx
lea rdx,[rdi+OPB.DeviceInterface]
lea r8,[rdi+OPB.Path1]
mov dword [r8+DeviceInterfaceDetailData.cbSize],8
xor eax,eax
push rax rax   
sub rsp,32
call [SetupDiGetDeviceInterfaceDetailA]                                   ; #4
add rsp,32+16
test rax,rax
jz BreakEnumeration
;--- Open device as file for IOCTL operations ---
; Parm#1 = RCX = Pointer to name string
; Parm#2 = RDX = File access mode
; Parm#3 = R8  = File sharing mode
; Parm#4 = R9  = Security attributes
; Parm#5 = File create disposition
; Parm#6 = File attributes
; Parm#7 = Template file handle
;---
lea rcx,[rdi+OPB.Path1.DevicePath]
mov edx,facc
mov r8d,fshr
mov r9d,fsec
push 0 ftpl fatr fcdp
sub rsp,32
call [CreateFile]                                                         ; #5
add rsp,32+32
test rax,rax
jz BreakEnumeration
xchg r15,rax                                            ; R15 = Battery handle
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
mov rcx,r15
mov edx,IOCTL_BATTERY_QUERY_TAG
lea r8,[rdi+OPB.ShortInBuf]
mov r9d,4
push 0                                 ; Parm#8
lea rax,[rdi+OPB.OutRet]
push rax                               ; Parm#7
mov dword [rax],0
push 4                                 ; Parm#6
lea rax,[rdi+OPB.InBuf.BatteryTag]
push rax                               ; Parm#5
sub rsp,32
call [DeviceIoControl]                                                    ; #6
add rsp,32+32
test rax,rax
jz BreakTag
cmp dword [rdi+OPB.OutRet],0 
je BreakTag
;--- Begin big conditional block ---
;--- Battery model string ---
lea r14,[rdi+OPB.StatusBatteryModel]
mov r13d,128
mov eax,BatteryDeviceName
call IoctlRequest                                                         ; #7
;--- Battery vendor string ---
lea r14,[rdi+OPB.StatusBatteryVendor]
mov r13d,128
mov eax,BatteryManufactureName
call IoctlRequest                                                         ; #8
;--- Battery manufacture date as number and as string ---
lea r14,[rdi+OPB.StatusBatteryDate]
mov r13d,16
mov eax,BatteryManufactureDate
call IoctlRequest                                                         ; #9
;--- Battery serial number string ---
lea r14,[rdi+OPB.StatusBatterySnum]
mov r13d,128
mov eax,BatterySerialNumber
call IoctlRequest                                                         ; #10
;--- Battery unique id string ---
lea r14,[rdi+OPB.StatusBatteryUid]
mov r13d,128
mov eax,BatteryUniqueId
call IoctlRequest                                                         ; #11
;--- Battery chemistry, as part of battery info ---
lea r14,[rdi+OPB.StatusBatteryInfo]
mov r13d,64
mov eax,BatteryInformation
call IoctlRequest                                                         ; #12
;--- Battery temperature ---
lea r14,[rdi+OPB.StatusBatteryTemp]
mov r13d,16
mov eax,BatteryTemperature
call IoctlRequest                                                         ; #13
;--- Battery status read, for next 4 parameters ---
mov rcx,r15
mov edx,IOCTL_BATTERY_QUERY_STATUS
lea r8,[rdi+OPB.WaitStatus]
mov eax,[rdi+OPB.InBuf.BatteryTag]
mov [r8+BATTERY_WAIT_STATUS.BatteryTag],eax
xor eax,eax
mov [r8+BATTERY_WAIT_STATUS.Timeout],eax
mov [r8+BATTERY_WAIT_STATUS.PowerState],eax
mov [r8+BATTERY_WAIT_STATUS.LowCapacity],eax
mov [r8+BATTERY_WAIT_STATUS.HighCapacity],eax
mov r9d,20
push 0                                 ; Parm#8
lea rax,[rdi+OPB.StatusBatteryStatus+08]
mov qword [rax],0
push rax                               ; Parm#7
push 16                                ; Parm#6
lea rax,[rdi+OPB.StatusBatteryStatus+16]
push rax                               ; Parm#5
sub rsp,32
call [DeviceIoControl]                                                    ; #14
add rsp,32+32
mov [rdi+OPB.StatusBatteryStatus+00],rax
;--- End of big conditional block ---
BreakTag:
;--- Cycle for maximum 10 batteries ---
add rdi,2048
inc esi
cmp esi,10
jb CycleEnumeration
;--- Exit point for errors and normal cycle termination ---
BreakEnumeration:
mov [r12],rsi
mov rsp,rbp
pop r15 r14 r13 r12 rbp rdi rsi rbx


;---------- Exit --------------------------------------------------------------;

xor ecx,ecx                   ; RCX=0, exit code
call [ExitProcess]            ; Exit from application


;---------- Subroutine --------------------------------------------------------;

;-----------------------------------------------------;
; INPUT:   EAX = IOCTL Request                        ;
;          RDI = Base (statical) OPB Pointer          ;
;          R15 = Battery handle                       ; 
;          R14 = Current (dynamical) OPB Pointer      ;
;          R13 = Output data size                     ;
;          RSP = Must be aligned 16                   ;  
; OUTPUT:  OPB updated at [R14]                       ;
;-----------------------------------------------------;
IoctlRequest:
push rax          ; For stack alignment after call subroutine
mov rcx,r15
mov edx,IOCTL_BATTERY_QUERY_INFORMATION
lea r8,[rdi+OPB.InBuf]
mov [r8+BATTERY_QUERY_INFORMATION.InformationLevel],eax
mov r9d,12
push 0                                 ; Parm#8
lea rax,[r14+08]
mov qword [rax],0
push rax                               ; Parm#7
push r13                               ; Parm#6
lea rax,[r14+16]
push rax                               ; Parm#5
sub rsp,32
call [DeviceIoControl]
add rsp,32+32
mov [r14+00],rax
pop rax
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
rsvd       dq ?             ; Reserved
ends
;---
struct DeviceInfoData
cbSize     dd ?             ; Struct. size, caller must set
classguid  db 16 dup (?)    ; Interface class GUID
devinst    dd ?             ; Handle to device instance (devnode)
rsvd       dq ?             ; Reserved
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



