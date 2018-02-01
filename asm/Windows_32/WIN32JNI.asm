include 'win32a.inc'
format PE GUI 4.0 DLL
entry DllMain

section '.text' code readable executable

DllMain:        ; This called by Operating System when load DLL
mov eax,1       ; Return status to OS caller
ret

;BoxOutput:
;call VisualDump
;ret 4*4


;---------- NATIVE AGENT ------------------------------------------------------;

; Parm#1 = [ESP+04] = JNI Environment  
; Parm#2 = [ESP+08] = RDX = JNI This Object reference (not used by this routine)
; Parm#3 = [ESP+12] = Object IPB array of qwords (long) reference or NULL
; Parm#4 = [ESP+16] = Object OPB array of qwords (long) reference or NULL
; Parm#5 = [ESP+20] = IPB size, qwords, or function code if IPB=NULL
; Parm#6 = [ESP+24] = OPB size, qwords, or reserved if OPB=NULL
; Return  EAX = JNI Status, 0=Error, 1=IA32 OK, 2=x64 OK
; Remember about 6*4=24 bytes must be removed from stack when return (RET 24),
; because required by IA32 calling convention.

NativeAgent:
push ebx esi edi ebp                   ; Save non-volatile registers
xor eax,eax
push eax eax                           ; Reserve space for variables
mov ebp,esp                            ; EBP=Frame, parm #1 at [ebp+28]
xor esi,esi                            ; Pre-blank IPB pointer
xor edi,edi                            ; Pre-blank OPB pointer
;--- Check IPB presence ---
mov ecx,[ebp+36]                       ; ECX = IPB object
jecxz @f                               ; Go skip IPB extraction if IPB=null
mov ebx,[ebp+28]                       ; EBX = environment
mov eax,[ebx]                          ; EAX = Pointer to functions table
push ebp ecx ebx  
;--- Get IPB, parms: env, IPB Object, Pointer to flag ---
call dword [eax+188*4]                 ; JNI call [GetLongArrayElements]
test eax,eax
jz StatusRet                           ; Go skip if error = NULL pointer
xchg esi,eax                           ; ESI = Pointer to IPB
@@:
;--- Check OPB presence ---
mov ecx,[ebp+40]                       ; ECX = OPB object
jecxz @f                               ; Go skip IPB extraction if OPB=null
mov ebx,[ebp+28]                       ; EBX = environment
mov eax,[ebx]                          ; EAX = Pointer to functions table
lea edx,[ebp+4]
push edx ecx ebx  
;--- Get OPB, parms: env, OPB Object, Pointer to flag ---
call dword [eax+188*4]                 ; JNI call [GetLongArrayElements]
test eax,eax
jz StatusRet                           ; Go skip if error = NULL pointer
xchg edi,eax                           ; ESI = Pointer to IPB
@@:
;--- Target operation ---
test esi,esi
jz IPB_null
;--- Return point ---
ReleaseRet:
;--- Check IPB release requirement flag and IPB presence ---
cmp dword [ebp],0
je @f                                  ; Go skip if IPB release not required
mov ecx,[ebp+36]                       ; ECX = IPB object
jecxz @f                               ; Go skip IPB release if IPB=null
mov ebx,[ebp+28]                       ; EBX = environment
mov eax,[ebx]                          ; EAX = Pointer to functions table
push 0 esi ecx ebx  
;--- Release IPB, parms: env, obj, Pointer, Release mode --- 
call dword [eax+196*4]                 ; call [ReleaseLongArrayElements]
@@:
;--- Check OPB release requirement flag and OPB presence ---
cmp dword [ebp+4],0
je @f                                  ; Go skip if OPB release not required
mov ecx,[ebp+40]                       ; EDX = OPB object
jecxz @f                               ; Go skip OPB release if OPB=null
mov ebx,[ebp+28]                       ; EBX = environment
mov eax,[ebx]                          ; EAX = Pointer to functions table
push 0 edi ecx ebx  
;--- Release OPB, parms: env, obj, Pointer, Release mode --- 
call dword [eax+196*4]                 ; call [ReleaseLongArrayElements]
@@:
;--- Return with status = EAX ---
mov eax,1                              ; RAX=1 (true) means OK from Win32 DLL 
StatusRet:                             ; Entry point with RAX=0 (error)
pop ecx ecx ebp edi esi ebx            ; Restore non-volatile registers
ret 24                                 ; Return to Java JNI service caller 


;---------- HANDLERS DETECT FOR CASE IPB=NULL ---------------------------------;

IPB_null:
xor eax,eax
mov ecx,[ebp+44]
cmp ecx,FunctionCount
jae ReleaseRet
jmp dword [FunctionSelector+ecx*4]


;---------- HANDLERS FOR GET SYSTEM INFO --------------------------------------;

FncGetSystemInfo:           ; This for Win32
push edi                    ; Parm#1 = Pointer to output buffer
call [GetSystemInfo]
jmp ReleaseRet 

FncGetNativeSystemInfo:     ; This for WoW64 
push ebx esi
push LibName
call [GetModuleHandle]     ; Get handle for library: KERNEL32.DLL
test eax,eax
jz @f
xchg esi,eax
push Fn1Name esi
call [GetProcAddress]      ; Get address for procedure: IsWow64Process
test eax,eax
jz @f
xchg ebx,eax
call [GetCurrentProcess]    ; Get handle of current process
push 0
mov ecx,esp
push ecx eax
call ebx                    ; Call function: IsWow64Process 
pop eax
@@:
test eax,eax
mov dword [edi],0FFFFFFFFh 
jz @f
push Fn2Name esi
call [GetProcAddress]      ; Get address for procedure: GetNativeSystemInfo
test eax,eax
jz @f
push edi
call eax                    ; Call function: GetNativeSystemInfo
@@:
pop esi ebx
jmp ReleaseRet 

;---------- HANDLER FOR GET SYSTEM POWER STATUS -------------------------------;

FncGetSystemPowerStatus:
lea ecx,[edi+8]
push ecx
call [GetSystemPowerStatus]
mov [edi],eax
mov dword [edi+4],0
jmp ReleaseRet

;---------- HANDLER FOR GET BATTERY DETAILS -----------------------------------;

FncGetBatteryDetails:
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

; *** NORMAL ***
push esi GUID_DEVCLASS_BATTERY 0 ebx      ; Parm#4, Parm#3, Parm#2, Parm#1
;*** DEBUG ***
; mov eax,esi
; and eax,0FFFFFFFEh
; push eax GUID_DEVCLASS_BATTERY 0 ebx      ; Parm#4, Parm#3, Parm#2, Parm#1 
;*** END DEBUG ***

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
;--- Exit ---
jmp ReleaseRet

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

;---------- HANDLER FOR MEASURE CPU CLOCK -------------------------------------;

FncMeasureCpuClock:
call GetCPUCLK
jmp ReleaseRet

include 'cpu.inc'

;---------- DEBUG: This fragment for experiments with callbacks ---------------;
;
;_DEBUG:
;push eax
;call VisualDump
;pop eax
;ret
;
;VisualDump:
;push eax ebx ecx edx esp ebp esi edi
;;---
;cld
;mov ecx,8
;lea esi,[_Message]
;lea edi,[_Buffer]
;lea ebp,[esp+7*4]
;DumpRegs:
;movsw
;movsb
;mov eax,' =  '
;stosd
;mov eax,[ebp]
;sub ebp,4 
;call HexPrint32
;mov ax,0D0Ah
;stosw
;loop DumpRegs
;mov al,0
;stosb
;;---
;xor eax,eax
;push eax                    ; Parm #4 = Message flags
;push dword _Caption         ; Parm #3 = Caption (upper message)
;push dword _Buffer          ; Parm #2 = Message
;push eax                    ; Parm #1 = Parent window
;call [MessageBoxA]          ; Call target function - show window
;;---
;pop edi esi ebp eax edx ecx ebx eax
;mov eax,10                  ; Return code = 10
;ret

;---------- Print 32-bit Hex Number ---------------------------;
; INPUT:  EAX = Number                                         ;
;         EDI = Destination Pointer                            ;
; OUTPUT: EDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint32:
;push eax
;ror eax,16
;call HexPrint16
;pop eax
; no RET, continue at next subroutine
;---------- Print 16-bit Hex Number ---------------------------;
; INPUT:  AX  = Number                                         ;
;         EDI = Destination Pointer                            ;
; OUTPUT: EDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint16:
;push eax
;xchg al,ah
;call HexPrint8
;pop eax
; no RET, continue at next subroutine
;---------- Print 8-bit Hex Number ----------------------------;
; INPUT:  AL  = Number                                         ;
;         EDI = Destination Pointer                            ;
; OUTPUT: EDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint8:
;push eax
;ror al,4
;call HexPrint4
;pop eax
; no RET, continue at next subroutine
;---------- Print 4-bit Hex Number ----------------------------;
; INPUT:  AL  = Number (bits 0-3)                              ;
;         EDI = Destination Pointer                            ;
; OUTPUT: EDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint4:
;cld
;push eax
;and al,0Fh
;cmp al,9
;ja HP4AF
;add al,'0'
;jmp HP4Store
;HP4AF:
;add al,'A'-10
;HP4Store:
;stosb
;pop eax
;ret

section '.data' data readable writeable
; Remember about error if data section empty
;_Caption  DB '  GPR32 dump',0
;_Message  DB 'EAXEBXECXEDXESPEBPESIEDI'
;_Buffer   DB 1024 DUP (?)

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
Chemistry            db 4 dup (?) ; ASCII
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
; Disabled, because make EDI+16
; ItemsCount           dq ?
; Reserved1            dq ?
;--- Start per-battery data --- 1104 bytes --- 138 qwords ---  
cbRequired           dd ?
OutRet               dd ?
ShortInBuf           dd ?
DeviceInterface      DeviceInterfaceData        ; 32 bytes
Path1                DeviceInterfaceDetailData  ; 1028 bytes --- qword #6 - 1/2
InBuf                BATTERY_QUERY_INFORMATION  ; 12 bytes
WaitStatus           BATTERY_WAIT_STATUS        ; 20 bytes
;--- Battery Model --- 128 bytes --- 16 qwords --- qword #140 --- 
StatusBatteryModel   dq ?
SizeBatteryModel     dq ?
OutBufBatteryModel   db 112 dup (?) 
;--- Battery Vendor --- 128 bytes --- 16 qwords --- qword #156 ---
StatusBatteryVendor  dq ?
SizeBatteryVendor    dq ?
OutBufBatteryVendor  db 112 dup (?) 
;--- Battery Manufacture Date --- 32 bytes --- 4 qwords ---  qword #172 ---
StatusBatteryDate    dq ?
SizeBatteryDate      dq ?
OutBufBatteryDate    db 16 dup (?) 
;--- Battery Serial Number as string --- 128 bytes --- 16 qw. --- qword #176 ---
StatusBatterySnum    dq ?
SizeBatterySnum      dq ?
OutBufBatterySnum    db 112 dup (?) 
;--- Battery Unique ID --- 128 bytes --- 16 qwords --- qword #192 ---
StatusBatteryUid     dq ?
SizeBatteryUid       dq ?
OutBufBatteryUid     db 112 dup (?) 
;--- Battery Info --- 64 bytes --- 8 qwords --- qword #208 ---
StatusBatteryInfo    dq ?
SizeBatteryInfo      dq ?
OutBufBatteryInfo    db 48 dup (?) 
;--- Battery Temperature --- 32 bytes --- 4 qwords --- qword #216 ---
StatusBatteryTemp    dq ?
SizeBatteryTemp      dq ?
OutBufBatteryTemp    db 16 dup (?) 
;--- Battery Status --- 32 bytes --- 4 qwords --- qword #220 ---
StatusBatteryStatus  dq ?
SizeBatteryStatus    dq ?
OutBufBatteryStatus  db 16 dup (?) 
;--- End ---
ends

FunctionCount     =   5
FunctionSelector  DD  FncGetSystemInfo
                  DD  FncGetSystemPowerStatus
                  DD  FncGetBatteryDetails
                  DD  FncMeasureCpuClock
                  DD  FncGetNativeSystemInfo

;--- Data for detect WOW64 ---
LibName  DB  'KERNEL32',0
Fn1Name  DB  'IsWow64Process',0
Fn2Name  DB  'GetNativeSystemInfo',0

section '.edata' export data readable
export 'WIN32JNI.dll',\
NativeAgent , 'Java_powerinfo_PAL_NativeAgent'    ; ENABLE THIS FOR PRODUCT
; NativeAgent , 'Java_JNIGATE_NativeAgent'        ; ENABLE THIS FOR DEBUG

;section '.edata' export data readable
;export 'WIN32JNI.dll',\
;BoxOutput   , 'Java_JNIGATE_Dump',\
;NativeAgent , 'Java_JNIGATE_NativeAgent'

;section '.idata' import data readable writeable
;library kernel32, 'KERNEL32.DLL'  ; , user32,'USER32.DLL'    
;include 'api\kernel32.inc'
; include 'api\user32.inc'        ; USER32.DLL required if MessageBoxA used

section '.idata' import data readable writeable
library kernel32,'KERNEL32.DLL', setupapi, 'SETUPAPI.DLL'
include 'api\kernel32.inc'

import setupapi, \
SetupDiGetClassDevsA, 'SetupDiGetClassDevsA', \
SetupDiEnumDeviceInterfaces, 'SetupDiEnumDeviceInterfaces', \
SetupDiGetDeviceInterfaceDetailA, 'SetupDiGetDeviceInterfaceDetailA'

data fixups
end data

