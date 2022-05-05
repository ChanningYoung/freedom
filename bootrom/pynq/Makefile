bootrom_img = bootrom.img

EXPORT_CC_PATH := export PATH=$(COMPILER_PATH):$$PATH

# CROSS_COMPILE := riscv64-unknown-linux-gnu-
CROSS_COMPILE := riscv64-unknown-elf-

CC := $(CROSS_COMPILE)gcc
OBJCOPY := $(CROSS_COMPILE)objcopy
OBJDUMP := $(CROSS_COMPILE)objdump

BUILD_DIR= ../../builds/pynqz2-ictfreedom
CONFIG_PROJECT=ictfreedom.system
CONFIG=PYNQFPGAConfig
dtb := $(BUILD_DIR)/$(CONFIG_PROJECT).$(CONFIG).dtb

all: $(bootrom_img)

%.img: %.bin
	dd if=$< of=$@ bs=2048 count=4

%.bin: %.elf
	$(EXPORT_CC_PATH) && $(OBJCOPY) -O binary $< $@

%.elf: %.S linker.ld $(dtb)
	@cp $(dtb) ./system.dtb
	$(EXPORT_CC_PATH) && $(CC) -Tlinker.ld $< main.c -nostdlib -static -Wl,--no-gc-sections -o $@
	$(EXPORT_CC_PATH) && $(OBJDUMP) -D $@ > bootrom.txt 
	@rm system.dtb

$(dtb): $(BUILD_DIR)/$(CONFIG_PROJECT).$(CONFIG).dts
	dtc -I dts -O dtb -o $@ $<

clean:
	@rm -f *.dtb *.img *.elf *.bin
