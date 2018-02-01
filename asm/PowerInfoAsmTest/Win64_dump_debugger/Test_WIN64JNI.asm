include 'win64a.inc'
format PE64 GUI 5.0
entry start

section '.text' code readable executable
start:
sub rsp,8*5                   ; Create and align stack frame
mov ecx,1                     ; Parm#1 = RCX, this also clear bits RCX.[63-32]
mov edx,1                     ; Parm#2 = RDX, this also clear bits RDX.[63-32]
mov r8,3                      ; Parm#3 = R8
mov r9,7                      ; Parm#4 = R9
call [BoxOutput]              ; Call imported target function
xor ecx,ecx                   ; RCX=0, exit code
call [ExitProcess]            ; Exit from application

section '.data' data readable writeable
String1  DB  0                ; Remember about error if data section empty

section '.idata' import data readable writeable
library kernel32,'KERNEL32.DLL',WIN64JNI,'WIN64JNI.DLL'
include 'api\kernel32.inc'    ; KERNEL32.DLL required because ExitProcess used

import WIN64JNI, \            ; This library.function also used from Java
BoxOutput , 'Java_JNIGATE_Dump'

