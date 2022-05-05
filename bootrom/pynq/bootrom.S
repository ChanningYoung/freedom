#define DRAM_BASE 0x50000000
#define STACK_BASE 0x5ffffff0
#define PWMTEST 0x2000
.section .text.start, "ax", @progbits
.globl _start
_start:
  li sp,STACK_BASE
  li a0,PWMTEST
  sw sp,(a0)
  call _hello
  li s0, DRAM_BASE
  csrr a0, mhartid
  la a1, _dtb
  jr s0

.section .text.hang, "ax", @progbits
.globl _hang
_hang:
  la a0, _start
  csrw mtvec, a0
  csrr a0, mhartid
  la a1, _dtb
1:
  wfi
  j 1b

.section .rodata.dtb, "a", @progbits
.globl _dtb
_dtb:
.incbin "system.dtb"
.byte 0
