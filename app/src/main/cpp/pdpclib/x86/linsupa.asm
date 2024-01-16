# linsupa.asm - support code for C programs for Linux
#
# This program written by Paul Edwards
# Released to the public domain


.globl ___setj
___setj:
.globl __setj
__setj:
mov 4(%esp), %eax
push %ebx
mov %esp, %ebx
mov %ebx, 20(%eax) #esp

mov %ebp, %ebx
mov %ebx, 24(%eax)

mov %ecx, 4(%eax)
mov %edx, 8(%eax)
mov %edi, 12(%eax)
mov %esi, 16(%eax)

mov 4(%esp), %ebx    # return address
mov %ebx, 28(%eax)   # return address

pop %ebx
mov %ebx,0(%eax)
mov $0, %eax

ret



.globl ___longj
___longj:
.globl __longj
__longj:
mov 4(%esp), %eax
mov 20(%eax), %ebp
mov %ebp, %esp

pop %ebx            # position of old ebx
pop %ebx            # position of old return address

mov 28(%eax), %ebx  # return address
push %ebx

mov 24(%eax), %ebx
mov %ebx, %ebp

mov 0(%eax), %ebx
mov 4(%eax), %ecx
mov 8(%eax), %edx
mov 12(%eax), %edi
mov 16(%eax), %esi

mov 60(%eax), %eax    # return value

ret


.globl ___write
___write:
.globl __write
__write:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx
push %edx

# function code 4 = write
movl $4, %eax
# handle
movl 8(%ebp), %ebx
# data pointer
movl 12(%ebp), %ecx
# length
movl 16(%ebp), %edx
int $0x80
pop %edx
pop %ecx
pop %ebx
pop %ebp
ret


.globl ___read
___read:
.globl __read
__read:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx
push %edx

# function code 3 = read
movl $3, %eax
# handle
movl 8(%ebp), %ebx
# data pointer
movl 12(%ebp), %ecx
# length
movl 16(%ebp), %edx
int $0x80
pop %edx
pop %ecx
pop %ebx
pop %ebp
ret



.globl ___open
___open:
.globl __open
__open:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx
push %edx

# function code 5 = open
movl $5, %eax
# filename
movl 8(%ebp), %ebx
# flag
movl 12(%ebp), %ecx
# mode
movl 16(%ebp), %edx
int $0x80
pop %edx
pop %ecx
pop %ebx
pop %ebp
ret



.globl __seek
__seek:
.globl ___seek
___seek:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx
push %edx

# function code 19 = lseek
movl $19, %eax
# handle
movl 8(%ebp), %ebx
# offset
movl 12(%ebp), %ecx
# whence
movl 16(%ebp), %edx
int $0x80
pop %edx
pop %ecx
pop %ebx
pop %ebp
ret



.globl ___rename
___rename:
.globl __rename
__rename:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx

# function code 38 = rename
movl $38, %eax
# old file
movl 8(%ebp), %ebx
# new file
movl 12(%ebp), %ecx
int $0x80
pop %ecx
pop %ebx
pop %ebp


.globl ___remove
___remove:
.globl __remove
__remove:
push %ebp
mov %esp, %ebp
push %ebx
# function code 10 = unlink
movl $10, %eax
# filename
movl 8(%ebp), %ebx
int $0x80
pop %ebx
pop %ebp
ret


.globl ___close
___close:
.globl __close
__close:
push %ebp
mov %esp, %ebp
push %ebx
# function code 6 = close
movl $6, %eax
# handle
movl 8(%ebp), %ebx
int $0x80
pop %ebx
pop %ebp
ret


.globl ___exita
___exita:
.globl __exita
__exita:
# exit/terminate
push %ebp
mov %esp, %ebp
push %ebx
movl 8(%ebp), %ebx
movl $1, %eax
int $0x80
pop %ebx
pop %ebp
ret


.globl ___time
___time:
.globl __time
__time:
push %ebp
mov %esp, %ebp
push %ebx
# function code 13 = retrieve current time
movl $13, %eax
# pointer to time_t
movl 8(%ebp), %ebx
int $0x80
pop %ebx
pop %ebp
ret


.globl ___ioctl
___ioctl:
.globl __ioctl
__ioctl:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx
push %edx
# function code 54 = ioctl
movl $54, %eax
# file descriptor
movl 8(%ebp), %ebx
# command
movl 12(%ebp), %ecx
# parameter
movl 16(%ebp), %edx
int $0x80
pop %edx
pop %ecx
pop %ebx
pop %ebp
ret


.globl ___chdir
___chdir:
.globl __chdir
__chdir:
push %ebp
mov %esp, %ebp
push %ebx
# function code 12 = chdir
movl $12, %eax
# filename (directory name)
movl 8(%ebp), %ebx
int $0x80
pop %ebx
pop %ebp
ret


.globl ___rmdir
___rmdir:
.globl __rmdir
__rmdir:
push %ebp
mov %esp, %ebp
push %ebx
# function code 40 = rmdir
movl $40, %eax
# pathname
movl 8(%ebp), %ebx
int $0x80
pop %ebx
pop %ebp
ret


.globl ___mkdir
___mkdir:
.globl __mkdir
__mkdir:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx
# function code 39 = mkdir
movl $39, %eax
# pathname
movl 8(%ebp), %ebx
# mode
movl 12(%ebp), %ecx
int $0x80
pop %ecx
pop %ebx
pop %ebp
ret


.globl ___getdents
___getdents:
.globl __getdents
__getdents:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx
push %edx
# function code 141 = getdents
movl $141, %eax
# file descriptor
movl 8(%ebp), %ebx
# dirent
movl 12(%ebp), %ecx
# count
movl 16(%ebp), %edx
int $0x80
pop %edx
pop %ecx
pop %ebx
pop %ebp
ret


.globl ___mprotect
___mprotect:
.globl __mprotect
__mprotect:
push %ebp
mov %esp, %ebp
push %ebx
push %ecx
push %edx
# function code 125 = mprotect
movl $125, %eax
# start
movl 8(%ebp), %ebx
# len
movl 12(%ebp), %ecx
# prot
movl 16(%ebp), %edx
int $0x80
pop %edx
pop %ecx
pop %ebx
pop %ebp
ret
