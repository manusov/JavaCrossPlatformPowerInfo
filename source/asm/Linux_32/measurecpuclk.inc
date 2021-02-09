;--- Measure CPU TSC (Time Stamp Counter) clock frequency ---------------;
;                                                                        ;
; INPUT:   None                                                          ;
;                                                                        ;
; OUTPUT:  CF flag = Status: 0(NC)=Measured OK, 1(C)=Measurement error	 ;
;          Output RAX,RDX valid only if CF=0(NC)                         ;
;          EDX:EAX = TSC Frequency, Hz, F = Delta TSC per 1 second       ;
;------------------------------------------------------------------------;
MeasureCpuClk:
push edi esi ebp

;--- Prepare parameters, early to minimize dTSC ---
; lea ebx,[TimespecWait]  ; EBX = Pointer to loaded wait time: DQ sec, ns
sub esp,32
mov ebx,esp
lea ecx,[ebx+16]          ; ECX = Pointer to stored remain time: DQ sec, ns
xor eax,eax
mov dword [ebx+00],1
mov dword [ebx+04],eax 
mov dword [ebx+08],eax
mov dword [ebx+12],eax 
mov dword [ecx+00],eax
mov dword [ecx+04],eax 
mov dword [ecx+08],eax
mov dword [ecx+12],eax 

;--- Get TSC value before 1 second pause ---
rdtsc                     ; EDX:EAX = TSC, EDX = High , EAX = Low
push eax edx
;--- Wait 1 second ---
mov eax,162               ; EAX = Linux API function (syscall number) = SYS_NANOSLEEP
push ecx
int 80h
pop ecx
xchg ebx,eax
;--- Get TSC value after 1 second pause ---
rdtsc                     ; EDX:EAX = TSC, EDX = High , EAX = Low , BEFORE 1 second pause
pop edi esi               ; EDI:ESI = TSC, ECX = High , EBX = Low , AFTER 1 second pause
;--- Check results ---
test ebx,ebx
jnz TimerFailed           ; Go if error returned or wait interrupted
mov ebx,[ecx+00]          ; Time remain, seconds
or ebx,[ecx+04]
or ebx,[ecx+08]           ; Disjunction with Time remain, nanoseconds
or ebx,[ecx+12]
jnz TimerFailed           ; Go if remain time stored by function
;--- Calculate delta-TSC per 1 second = TSC frequency ---
sub eax,esi               ; Subtract: DeltaTSC.Low  = EndTSC.Low - StartTSC.Low
sbb edx,edi               ; Subtract: DeltaTSC.High = EndTSC.High - StartTSC.High - Borrow
test edx,edx
jnz TimerFailed           ; This debug 32-bit code not supports > 4GHz

;--- Exit points ---
add esp,32
clc
TimerDone:
pop ebp esi edi
ret

TimerFailed:
add esp,32
stc
jmp TimerDone


