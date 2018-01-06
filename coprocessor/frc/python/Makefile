# Makefile for all C++ Examples

all: build

clean:
	cd ntcore && $(MAKE) clean && cd ..
	cd cscore && $(MAKE) clean && cd ..
	cd navx_vmxpi && $(MAKE) clean && cd ..

build:
	cd ntcore && $(MAKE) && cd ..
	cd cscore && $(MAKE) && cd ..
	cd navx_vmxpi && $(MAKE) && cd ..

run:
	cd ntcore && $(MAKE) run && cd ..
	cd cscore && $(MAKE) run && cd ..
	cd navx_vmxpi && $(MAKE) run && cd ..

.PHONY: build clean run