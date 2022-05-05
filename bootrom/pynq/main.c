#include <stdint.h>

#define XUARTPS_CR_OFFSET   0x00000000
#define XUARTPS_MR_OFFSET   0x00000004
#define XUARTPS_BAUDGEN_OFFSET  0x00000018
#define BAUD_RATE_DIVIDER_OFFSET  0x00000034
#define XUARTPS_FIFO_OFFSET 0x00000030
#define UART0_BASE_ADDR 0xe0000000
#define XUARTPS_SR_OFFSET 0x0000002c

#define GPIO_BASE_ADDR 0xe0300000
#define GPIO_BTN_OFFSET 0x00000000
#define GPIO_BTN_0_MASK 0x00000001
#define GPIO_BTN_1_MASK 0x00000002
#define GPIO_BTN_3_MASK 0x00000004
#define GPIO_BTN_4_MASK 0x00000008
#define GPIO_SW_OFFSET 0x00000004
#define GPIO_SW_0_MASK 0x00000001
#define GPIO_SW_1_MASK 0x00000002

char banner[] = "RISC-V Execution in BootROM...\n\rPassing Control to RISC-V Berkeley Boot Loader(BBL)...\n\r";

char hangmsg[] = "Execution Paused\n\r";

#define PAUSE_REQUEST (*((volatile unsigned int *)(GPIO_BASE_ADDR + GPIO_SW_OFFSET)) & GPIO_SW_0_MASK)

static inline void _putc(char c){
    while((*((volatile unsigned char*)(UART0_BASE_ADDR + XUARTPS_SR_OFFSET)) & 0x10) != 0);
    *((volatile unsigned char*)(UART0_BASE_ADDR + XUARTPS_FIFO_OFFSET)) = c;
}

static inline void init_uart(){
    while((*((volatile unsigned char*)(UART0_BASE_ADDR + XUARTPS_SR_OFFSET)) & 0x8) == 0);
}

static void print(char *s) {
    char c;
    while (c = *s++)
        _putc(c);
}

__attribute__((section("print_hello"))) int _hello()
{
    init_uart();
    
    if (PAUSE_REQUEST) {
        print(hangmsg);
        while (PAUSE_REQUEST) {
            __asm__ volatile ("wfi");
        }
    }

    print(banner);

    return 0;
}
