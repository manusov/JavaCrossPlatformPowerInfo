include 'win64a.inc'
format PE64 GUI 4.0 DLL
entry DllMain

section '.text' code readable executable

DllMain:        ; This called by Operating System when load DLL
mov eax,1       ; Return status to OS caller
ret

BoxOutput:
call VisualDump
ret










;---------- This fragment for experiments with callbacks ----------------------;
VisualDump:
push rbx rsi rdi rbp r12 r13 r14 r15        ; Save non-volatile registers
push rsi rdi
push r15 r14 r13 r12 r11 r10 r9 r8
push rdi rsi rbp
lea r8,[rsp+8*13 + 64]    ; +64, include save for callbacks experiment 
push r8 rdx rcx rbx rax 
cld
mov ecx,16
lea rsi,[_Message]
lea rdi,[_Buffer]
DumpRegs:
movsw
movsb
mov eax,' =  '
stosd
pop rax
call HexPrint64
mov ax,0D0Ah
stosw
loop DumpRegs
mov al,0
stosb
;---
push rbp                    ; Save RBP
mov rbp,rsp                 ; Save RSP 
sub rsp,32                  ; Create parameters shadow
and rsp,0FFFFFFFFFFFFFFF0h  ; Align RSP required for API Call
xor ecx,ecx                 ; RCX = Parm #1 = Parent window
lea rdx,[_Buffer]           ; RDX = Parm #2 = Message
lea r8,[_Caption]           ; R8  = Parm #3 = Caption (upper message)
xor r9,r9                   ; R9  = Parm #4 = Message flags
call [MessageBoxA]          ; Call target function - show window
mov rsp,rbp                 ; Restore RSP
pop rbp                     ; Restore RBP
;---
pop rdi rsi
mov eax,10                  ; Return code = 10
pop r15 r14 r13 r12 rbp rdi rsi rbx   ; Restore after save for callback expe.
ret
;---------- Print 64-bit Hex Number ---------------------------;
; INPUT:  RAX = Number                                         ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint64:
push rax
ror rax,32
call HexPrint32
pop rax
; no RET, continue at next subroutine
;---------- Print 32-bit Hex Number ---------------------------;
; INPUT:  EAX = Number                                         ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint32:
push rax
ror eax,16
call HexPrint16
pop rax
; no RET, continue at next subroutine
;---------- Print 16-bit Hex Number ---------------------------;
; INPUT:  AX  = Number                                         ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint16:
push rax
xchg al,ah
call HexPrint8
pop rax
; no RET, continue at next subroutine
;---------- Print 8-bit Hex Number ----------------------------;
; INPUT:  AL  = Number                                         ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint8:
push rax
ror al,4
call HexPrint4
pop rax
; no RET, continue at next subroutine
;---------- Print 4-bit Hex Number ----------------------------;
; INPUT:  AL  = Number (bits 0-3)                              ;
;         RDI = Destination Pointer                            ;
; OUTPUT: RDI = Modify                                         ;
;--------------------------------------------------------------;
HexPrint4:
cld
push rax
and al,0Fh
cmp al,9
ja HP4AF
add al,'0'
jmp HP4Store
HP4AF:
add al,'A'-10
HP4Store:
stosb
pop rax
ret

section '.data' data readable writeable
; Remember about error if data section empty
_Caption  DB '  GPR64 dump',0
_Message  DB 'RAXRBXRCXRDXRSPRBPRSIRDI'
          DB 'R8 R9 R10R11R12R13R14R15'
_Buffer   DB 1024 DUP (?)

section '.edata' export data readable
export 'WIN64JNI.dll',\
BoxOutput , 'Java_JNIGATE_Dump'

section '.idata' import data readable writeable
library user32,'USER32.DLL'    
include 'api\user32.inc'       ; USER32.DLL required because MessageBoxA used

data fixups
end data