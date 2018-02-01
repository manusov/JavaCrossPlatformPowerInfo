
GetCPUCLK:
push eax ebx ecx edx
mov dword [edi+0],0
mov dword [edi+4],0
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
mov [edi+0],eax
mov [edi+4],edx
@@:
pop edx ecx ebx eax
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
push ebx ecx edx
;--- Check for ID bit writeable for "1" ---
mov ebx,21                ; EFLAGS bit number = 21
pushf                     ; In the 64-bit mode, push EFLAGS
pop eax
bts eax,ebx               ; Set EAX.21=1
push eax
popf                      ; Load EFLAGS with EFLAGS.21=1
pushf                     ; Store EFLAGS
pop eax                   ; Load EFLAGS to EAX
btr eax,ebx               ; Check EAX.21=1, Set EAX.21=0
jnc NoCpuId               ; Go error branch if cannot set EFLAGS.21=1
;--- Check for ID bit writeable for "0" ---
push eax
popf                      ; Load EFLAGS with EFLAGS.21=0
pushf                     ; Store EFLAGS
pop eax                   ; Load EFLAGS to EAX
btr eax,ebx               ; Check EAX.21=0
jc NoCpuId                ; Go if cannot set EFLAGS.21=0
;--- Execute CPUID function 0, returned result in EAX ---
xor eax,eax               ; EAX = Function number for CPUID instruction
cpuid                     ; Execute CPUID function 0
;--- Exit points ---
ExitCpuId:
pop edx ecx ebx
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
;          Output EAX, EBX, ECX, EDX valid only if CF=0(NC)              ;
;          EDX:EAX = TSC Frequency, Hz, F = Delta TSC per 1 second       ;
;------------------------------------------------------------------------;
MeasureCpuClk:
cld                             ; Clear direction, because STOS used
xor eax,eax
push esi edi ebp eax eax        ; Last EAX = Variable
;--- Start measure frequency, wait toggle ---
push ebp
call [GetSystemTimeAsFileTime]  ; Get current count
mov esi,[ebp]
@@:
push ebp
call [GetSystemTimeAsFileTime]  ; Get next count for wait 100 ns
cmp esi,[ebp]
je @b
;--- Start time point ---
mov esi,[ebp+0]
mov edi,[ebp+4]
add esi,10000000                
adc edi,0                       ; EDI:ESI = 10^7 * 100ns = 1 second
rdtsc
push eax edx                    ; Stack qword = 64-bit TSC at operation start
;--- Delay 1 second ---
@@:
push ebp
call [GetSystemTimeAsFileTime]  ; Get count for wait 1 second
cmp edi,[ebp+4]                 ; Compare high: target=edi and returned=[ebp+4] 
ja @b                           ; Go wait if target > returned, must 1 second
jb @f
cmp esi,[ebp+0]                 ; Compare low: target=esi and returned=[ebp+0] 
ja @b                           ; Go wait if target > returned, must 1 second
@@:
;--- Stop time point ---
rdtsc                           ; EDX:EAX = 64-bit TSC at operation end
pop ecx ebx
sub eax,ebx
sbb edx,ecx
;--- Exit ---
ExitCpuClk:
pop ebp ebp ebp edi esi          ; First POP EBP for RSP-8 only 
ret

