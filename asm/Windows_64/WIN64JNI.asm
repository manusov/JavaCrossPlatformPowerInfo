include 'win64a.inc'
format PE64 GUI 4.0 DLL
entry DllMain

section '.text' code readable executable

DllMain:        ; This called by Operating System when load DLL
mov eax,1       ; Return status to OS caller
ret

;---------- DEBUG -------------------------------------------------------------;
;BoxOutput:
;call VisualDump
;ret

;---------- NATIVE AGENT ------------------------------------------------------;

; Parm#1 = RCX = JNI Environment  
; Parm#2 = RDX = JNI This Object reference (not used by this routine)
; Parm#3 = R8  = Object IPB array of qwords (long) reference or NULL
; Parm#4 = R9  = Object OPB array of qwords (long) reference or NULL
; Parm#5 = [RSP+40] = IPB size, qwords, or function code if IPB=NULL
; Parm#6 = [RSP+48] = OPB size, qwords, or reserved if OPB=NULL
; Return  RAX = JNI Status, 0=Error, 1=IA32 OK, 2=x64 OK

NativeAgent:
push rbx rsi rdi rbp r12 r13 r14 r15   ; Save non-volatile registers
mov rbp,rsp                            ; Save RSP because stack alignment
xor eax,eax
push rax rax                           ; Storage for variable
mov rbx,rcx                            ; RBX = Environment
mov r12,r8                             ; R12 = Object: Input Parm. Block
mov r13,r9                             ; R13 = Object: Output Parm. Block 
mov r14,[rbp+64+8+32+0]                ; R14 = Length of IPB (parm#5)
mov r15,[rbp+64+8+32+8]                ; R15 = Length of OPB (parm#6)
and rsp,0FFFFFFFFFFFFFFF0h             ; Stack alignment by calling convention
sub rsp,32                             ; Parm. shadow by calling convention
xor esi,esi                            ; Pre-blank IPB pointer
xor edi,edi                            ; Pre-blank OPB pointer
;--- Check IPB presence ---
test r12,r12
jz @f                                  ; Go skip IPB extraction if IPB=null
mov rdx,r12
lea r8,[rbp-8]
mov rax,[rbx]                          ; RAX = Pointer to functions table
;--- Get IPB, parms: RCX=env, RDX=IPB Object, R8=Pointer to flag ---
call qword [rax+188*8]                 ; JNI call [GetLongArrayElements]
test rax,rax
jz StatusRet                           ; Go skip if error = NULL pointer
xchg rsi,rax                           ; RSI = Pointer to IPB
@@:
;--- Check OPB presence ---
test r13,r13
jz @f                                  ; Go skip IPB extraction if OPB=null
mov rcx,rbx
mov rdx,r13
lea r8,[rbp-16]
mov rax,[rbx]                          ; RAX = Pointer to functions table
;--- Get OPB, parms: RCX=env, RDX=OPB Object, R8=Pointer to flag ---
call qword [rax+188*8]                 ; JNI call [GetLongArrayElements]
test rax,rax
jz StatusRet                           ; Go skip if error = NULL pointer
xchg rdi,rax                           ; RSI = Pointer to OPB
@@: 
;--- Target operation ---
test rsi,rsi
jz IPB_null
;--- Return point ---
ReleaseRet:
;--- Check IPB release requirement flag and IPB presence ---
cmp qword [rbp-8],0
je @f                                  ; Go skip if IPB release not required
test r12,r12
jz @f                                  ; Go skip IPB extraction if IPB=null
mov rcx,rbx
mov rdx,r12
mov r8,rsi
xor r9d,r9d
mov rax,[rbx]                          ; RAX = Pointer to functions table
;--- Release IPB, parms: RCX=env, RDX=obj, R8=Pointer, R9=Release mode --- 
call qword [rax+196*8]                 ; call [ReleaseLongArrayElements]
@@:
;--- Check OPB release requirement flag and OPB presence ---
cmp qword [rbp-16],0
je @f                                  ; Go skip if OPB release not required
test r13,r13
jz @f                                  ; Go skip OPB extraction if OPB=null
mov rcx,rbx
mov rdx,r13
mov r8,rdi
xor r9d,r9d
mov rax,[rbx]                          ; RAX = Pointer to functions table
;--- Release OPB, parms: RCX=env, RDX=obj, R8=Pointer, R9=Release mode --- 
call qword [rax+196*8]                 ; call [ReleaseLongArrayElements]
@@:
;--- Return with status = RAX ---
mov eax,2                              ; RAX=1 (true) means OK from Win64 DLL 
StatusRet:                             ; Entry point with RAX=0 (error)
mov rsp,rbp                            ; Restore RSP after alignment
pop r15 r14 r13 r12 rbp rdi rsi rbx    ; Restore non-volatile registers
ret                                    ; Return to Java JNI service caller 


;---------- HANDLERS DETECT FOR CASE IPB=NULL ---------------------------------;

IPB_null:
xor eax,eax
cmp r14,FunctionCount
jae ReleaseRet
jmp qword [FunctionSelector+r14*8]


;---------- HANDLER FOR GET SYSTEM INFO ---------------------------------------;

FncGetSystemInfo:
mov rcx,rdi
call [GetSystemInfo]
jmp ReleaseRet 

;---------- HANDLER FOR GET SYSTEM POWER STATUS -------------------------------;

FncGetSystemPowerStatus:
lea rcx,[rdi+8]
call [GetSystemPowerStatus]
mov [rdi],rax
jmp ReleaseRet

;---------- HANDLER FOR GET BATTERY DETAILS -----------------------------------;

FncGetBatteryDetails:
push rbx rsi rdi rbp r12 r13 r14 r15
mov rbp,rsp
and rsp,0FFFFFFFFFFFFFFF0h
sub rsp,32
;--- Initializing output data, counter and destination pointer ---
xor esi,esi    ; ESI = member index = 0
mov r12,rdi    ; R12 = pointer storage
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

; *** DEBUG , force 2 devices ***
; and r9d,0FFFFFFFEh
; *** DEBUG ***

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


;*** DEBUG ***
; test esi,esi
; jz @f
; add dword [rdi+OPB.OutBufBatteryStatus+04],1000
; add dword [rdi+OPB.OutBufBatteryStatus+12],-1000
; @@:
;*** DEBUG ***


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
;--- Exit ---
jmp ReleaseRet

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

;---------- HANDLER FOR MEASURE CPU CLOCK -------------------------------------;

FncMeasureCpuClock:
call GetCPUCLK
jmp ReleaseRet

include 'cpu.inc'

;---------- DEBUG: This fragment for experiments with callbacks ---------------;
;
;_DEBUG:
;push rax rcx rdx r8 r9 r10 r11
;call VisualDump
;pop r11 r10 r9 r8 rdx rcx rax
;ret
;
;VisualDump:
;push rbx rsi rdi rbp r12 r13 r14 r15        ; Save non-volatile registers
;push rsi rdi
;push r15 r14 r13 r12 r11 r10 r9 r8
;push rdi rsi rbp
;lea r8,[rsp+8*13 + 64]    ; +64, include save for callbacks experiment 
;push r8 rdx rcx rbx rax 
;cld
;mov ecx,16
;lea rsi,[_Message]
;lea rdi,[_Buffer]
;DumpRegs:
;movsw
;movsb
;mov eax,' =  '
;stosd
;pop rax
;call HexPrint64
;mov ax,0D0Ah
;stosw
;loop DumpRegs
;mov al,0
;stosb
;;---
;push rbp                    ; Save RBP
;mov rbp,rsp                 ; Save RSP 
;sub rsp,32                  ; Create parameters shadow
;and rsp,0FFFFFFFFFFFFFFF0h  ; Align RSP required for API Call
;xor ecx,ecx                 ; RCX = Parm #1 = Parent window
;lea rdx,[_Buffer]           ; RDX = Parm #2 = Message
;lea r8,[_Caption]           ; R8  = Parm #3 = Caption (upper message)
;xor r9,r9                   ; R9  = Parm #4 = Message flags
;call [MessageBoxA]          ; Call target function - show window
;mov rsp,rbp                 ; Restore RSP
;pop rbp                     ; Restore RBP
;;---
;pop rdi rsi
;mov eax,10                  ; Return code = 10
;pop r15 r14 r13 r12 rbp rdi rsi rbx   ; Restore after save for callback expe.
;ret
;---------- Print 64-bit Hex Number ---------------------------;
; INPUT:  RAX = Number                                         ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint64:
;push rax
;ror rax,32
;call HexPrint32
;pop rax
; no RET, continue at next subroutine
;---------- Print 32-bit Hex Number ---------------------------;
; INPUT:  EAX = Number                                         ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint32:
;push rax
;ror eax,16
;call HexPrint16
;pop rax
; no RET, continue at next subroutine
;---------- Print 16-bit Hex Number ---------------------------;
; INPUT:  AX  = Number                                         ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint16:
;push rax
;xchg al,ah
;call HexPrint8
;pop rax
; no RET, continue at next subroutine
;---------- Print 8-bit Hex Number ----------------------------;
; INPUT:  AL  = Number                                         ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint8:
;push rax
;ror al,4
;call HexPrint4
;pop rax
; no RET, continue at next subroutine
;---------- Print 4-bit Hex Number ----------------------------;
; INPUT:  AL  = Number (bits 0-3)                              ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
;HexPrint4:
;cld
;push rax
;and al,0Fh
;cmp al,9
;ja HP4AF
;add al,'0'
;jmp HP4Store
;HP4AF:
;add al,'A'-10
;HP4Store:
;stosb
;pop rax
;ret

section '.data' data readable writeable
;_Caption  DB '  GPR64 dump',0
;_Message  DB 'RAXRBXRCXRDXRSPRBPRSIRDI'
;          DB 'R8 R9 R10R11R12R13R14R15'
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
; Disabled, because make RDI+16
; ItemsCount           dq ?
; Reserved1            dq ?
;--- Start per-battery data --- 1104 bytes --- 138 qwords ---  
cbRequired           dd ?
OutRet               dd ?
ShortInBuf           dd ?
DeviceInterface      DeviceInterfaceData        ; 32 bytes
Path1                DeviceInterfaceDetailData  ; 1028 bytes
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
;--- Battery Manufacture Date --- 32 bytes --- 4 qwords --- qword #172 ---
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

FunctionCount     =   4
FunctionSelector  DQ  FncGetSystemInfo           ; Function 0
                  DQ  FncGetSystemPowerStatus    ; 1
                  DQ  FncGetBatteryDetails       ; 2
                  DQ  FncMeasureCpuClock         ; 3

section '.edata' export data readable
export 'WIN64JNI.dll',\
NativeAgent , 'Java_powerinfo_PAL_NativeAgent'       ; ENABLE THIS IF PRODUCT
; NativeAgent , 'Java_JNIGATE_NativeAgent'           ; ENABLE THIS IF DEBUG

;BoxOutput   , 'Java_JNIGATE_Dump',\
;NativeAgent , 'Java_JNIGATE_NativeAgent'

;section '.idata' import data readable writeable
;library kernel32, 'KERNEL32.DLL'  ; , user32,'USER32.DLL'    
;include 'api\kernel32.inc'
;; include 'api\user32.inc'        ; USER32.DLL required if MessageBoxA used

section '.idata' import data readable writeable
library kernel32,'KERNEL32.DLL', setupapi, 'SETUPAPI.DLL'
include 'api\kernel32.inc'
import setupapi, \
SetupDiGetClassDevsA, 'SetupDiGetClassDevsA', \
SetupDiEnumDeviceInterfaces, 'SetupDiEnumDeviceInterfaces', \
SetupDiGetDeviceInterfaceDetailA, 'SetupDiGetDeviceInterfaceDetailA'

data fixups
end data

