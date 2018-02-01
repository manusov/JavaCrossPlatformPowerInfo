
GetCPUCLK:
push rax rbx rcx rdx
mov qword [rdi],0
call CheckCpuId
jc @f
cmp eax,1
jb @f
mov eax,1
cpuid
test dl,10h
jz @f
call MeasureCpuClk
jc @f
mov [rdi],rax
@@:
pop rdx rcx rbx rax
ret

;------------------------------------------------------------------------;
; Detect CPUID support, execute CPUID function #0.                       ;
; Note CPUID can be supported by CPU but locked by Virtual Monitor.      ;
; Note check bit EFLAGS.21 toggleable, it is CPUID support indicator.    ;
; Note probably wrong result if trace this subroutine code.              ;
;                                                                        ;
; INPUT:   None                                                          ;
;                                                                        ;
; OUTPUT:  CF flag = Status: 0(NC)=Support OK, 1(C)=Not supported        ;
;          EAX = Largest standard CPUID function supported,              ;
;                valid only if CF=0.                                     ;
;------------------------------------------------------------------------;
CheckCpuId:
push rbx rcx rdx
;--- Check for ID bit writeable for "1" ---
mov ebx,21                ; EFLAGS bit number = 21
pushf                     ; In the 64-bit mode, push RFLAGS
pop rax
bts eax,ebx               ; Set EAX.21=1
push rax
popf                      ; Load RFLAGS with RFLAGS.21=1
pushf                     ; Store RFLAGS
pop rax                   ; Load RFLAGS to RAX
btr eax,ebx               ; Check EAX.21=1, Set EAX.21=0
jnc NoCpuId               ; Go error branch if cannot set EFLAGS.21=1
;--- Check for ID bit writeable for "0" ---
push rax
popf                      ; Load RFLAGS with RFLAGS.21=0
pushf                     ; Store RFLAGS
pop rax                   ; Load RFLAGS to RAX
btr eax,ebx               ; Check EAX.21=0
jc NoCpuId                ; Go if cannot set EFLAGS.21=0
;--- Execute CPUID function 0, returned result in EAX ---
xor eax,eax               ; EAX = Function number for CPUID instruction
cpuid                     ; Execute CPUID function 0
;--- Exit points ---
ExitCpuId:
pop rdx rcx rbx
ret                       ; Return, at this point CF=0(NC) after XOR EAX,EAX
NoCpuId:
stc                       ; CF=1(C) means error
jmp ExitCpuId 

;------------------------------------------------------------------------;
; Measure CPU TSC (Time Stamp Counter) clock frequency, Hz               ;
;                                                                        ;
; INPUT:   None                                                          ;
;                                                                        ;
; OUTPUT:  CF flag = Status: 0(NC)=Measured OK, 1(C)=Measurement error	 ;
;          Output RAX,RDX valid only if CF=0(NC)                         ;
;          RAX = TSC Frequency, Hz, F = Delta TSC per 1 second           ;
;------------------------------------------------------------------------;
MeasureCpuClk:
cld                         ; Clear direction, because STOSQ used
push rbx rcx rdx rsi rbp r8 r9 r10 r11 rax	 ; R8-R11 because API, RAX = Var
mov rbp,rsp                 ; RBP used for restore RSP and addressing variables
and rsp,0FFFFFFFFFFFFFFF0h  ; Align stack (16)
sub rsp,32                  ; Make parameters shadow
;--- Start measure frequency, wait toggle ---
mov rcx,rbp
call [GetSystemTimeAsFileTime]  ; Get current count
mov rsi,[rbp]
@@:
mov rcx,rbp
call [GetSystemTimeAsFileTime]  ; Get next count for wait 100 ns
cmp rsi,[rbp]
je @b
;--- Start time point ---
mov rsi,[rbp]
add rsi,10000000                ; RSI = 10^7 * 100ns = 1 second
rdtsc
shl rdx,32
lea rbx,[rax+rdx]               ; RBX = 64-bit TSC at operation start
;--- Delay 1 second ---
@@:
mov rcx,rbp
call [GetSystemTimeAsFileTime]  ; Get count for wait 1 second
cmp rsi,[rbp]                   ; Compare target=rsi and returned=[rbp] 
ja @b                           ; Go wait if target > returned, must 1 second
;--- Stop time point ---
rdtsc
shl rdx,32
or rax,rdx                      ; RAX = 64-bit TSC at operation end
sub rax,rbx                     ; RAX = Delta TSC = frequency (1 second)
;--- Restore RSP, pop extra registers, exit ---
ExitCpuClk:
mov rsp,rbp                            ; Restore RSP after alignment and shadow
pop rbx r11 r10 r9 r8 rbp rsi rdx rcx rbx  ; First POP RBX for RSP-8 only 
ret

