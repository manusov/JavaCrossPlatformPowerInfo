include 'win32a.inc'
format PE GUI 4.0 DLL
entry DllMain

section '.text' code readable executable

DllMain:        ; This called by Operating System when load DLL
mov eax,1       ; Return status to OS caller
ret

BoxOutput:

lea edi,[MyData]
call FncGetNativeSystemInfo
mov eax,[edi] 

call VisualDump
ret 4*4

;---------- TARGET FRAGMENT ---------------------------------------------------;



FncGetNativeSystemInfo:

;--- Detect WOW64 ---
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
ret




;---------- This fragment for experiments with callbacks ----------------------;
VisualDump:
push eax ebx ecx edx esp ebp esi edi
;---
cld
mov ecx,8
lea esi,[_Message]
lea edi,[_Buffer]
lea ebp,[esp+7*4]
DumpRegs:
movsw
movsb
mov eax,' =  '
stosd
mov eax,[ebp]
sub ebp,4 
call HexPrint32
mov ax,0D0Ah
stosw
loop DumpRegs
mov al,0
stosb
;---
xor eax,eax
push eax                    ; Parm #4 = Message flags
push dword _Caption         ; Parm #3 = Caption (upper message)
push dword _Buffer          ; Parm #2 = Message
push eax                    ; Parm #1 = Parent window
call [MessageBoxA]          ; Call target function - show window
;---
pop edi esi ebp eax edx ecx ebx eax
mov eax,10                  ; Return code = 10
ret

;---------- Print 32-bit Hex Number ---------------------------;
; INPUT:  EAX = Number                                         ;
;         EDI = Destination Pointer                            ;
; OUTPUT: EDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint32:
push eax
ror eax,16
call HexPrint16
pop eax
; no RET, continue at next subroutine
;---------- Print 16-bit Hex Number ---------------------------;
; INPUT:  AX  = Number                                         ;
;         EDI = Destination Pointer                            ;
; OUTPUT: EDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint16:
push eax
xchg al,ah
call HexPrint8
pop eax
; no RET, continue at next subroutine
;---------- Print 8-bit Hex Number ----------------------------;
; INPUT:  AL  = Number                                         ;
;         EDI = Destination Pointer                            ;
; OUTPUT: EDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint8:
push eax
ror al,4
call HexPrint4
pop eax
; no RET, continue at next subroutine
;---------- Print 4-bit Hex Number ----------------------------;
; INPUT:  AL  = Number (bits 0-3)                              ;
;         EDI = Destination Pointer                            ;
; OUTPUT: EDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint4:
cld
push eax
and al,0Fh
cmp al,9
ja HP4AF
add al,'0'
jmp HP4Store
HP4AF:
add al,'A'-10
HP4Store:
stosb
pop eax
ret

section '.data' data readable writeable
; Remember about error if data section empty
_Caption  DB '  GPR32 dump',0
_Message  DB 'EAXEBXECXEDXESPEBPESIEDI'
_Buffer   DB 1024 DUP (?)

;--- Data for detect WOW64 ---
LibName  DB  'KERNEL32',0
Fn1Name  DB  'IsWow64Process',0
Fn2Name  DB  'GetNativeSystemInfo',0
;- debug -
MyData   DB  4096 DUP (0)

section '.edata' export data readable
export 'WIN32JNI.dll',\
BoxOutput , 'Java_JNIGATE_Dump'

section '.idata' import data readable writeable
library kernel32, 'KERNEL32.DLL', user32,'USER32.DLL'    
include 'api\user32.inc'       ; USER32.DLL required because MessageBoxA used
include 'api\kernel32.inc'     ; KERNEL32.DLL added when debug WoW64

data fixups
end data
