SECTIONS
{
    ROM_BASE = 0x10000; /* ... but actually position independent */

    . = ROM_BASE;
    .text.start : { *(.text.start) }
    . = ROM_BASE + 0x40;
    .text.hang : { *(.text.hang) }
    . = ROM_BASE + 0x60;
    .print_hello : { *(print_hello) }
    .text : { *(.text) }
    .data : { *(data) }
    . = ROM_BASE + 0x200;
    .rodata.dtb : { *(.rodata.dtb) }
}
