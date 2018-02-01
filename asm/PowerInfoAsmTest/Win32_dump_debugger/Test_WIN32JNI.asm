include 'win32a.inc'
format PE GUI 4.0
entry start

section '.text' code readable executable
start:

mov eax,1
mov ebx,2
mov ecx,22h
mov edx,33h
mov ebp,3
mov esi,15h
mov edi,25h

push 7                        ; Parm#4
push 3                        ; Parm#3
push 1                        ; Parm#2
push 1                        ; Parm#1
call [BoxOutput]              ; Call imported target function
push 0
call [ExitProcess]            ; Exit from application

section '.data' data readable writeable
String1  DB  0                ; Remember about error if data section empty

section '.idata' import data readable writeable
library kernel32,'KERNEL32.DLL',WIN32JNI,'WIN32JNI.DLL'
include 'api\kernel32.inc'    ; KERNEL32.DLL required because ExitProcess used

import WIN32JNI, \            ; This library.function also used from Java
BoxOutput , 'Java_JNIGATE_Dump'

