# Makefile for all C++ Examples

all: build

clean:
	cd nt_client && $(MAKE) clean && cd ..
	cd video_to_disk && $(MAKE) clean && cd ..
	cd navx_vmxpi && $(MAKE) clean && cd ..

build:
	cd nt_client && $(MAKE) && cd ..
	cd video_to_disk && $(MAKE) && cd ..
	cd navx_vmxpi && $(MAKE) && cd ..

run:
	cd nt_client && $(MAKE) run && cd ..
	cd video_to_disk && $(MAKE) run && cd ..
	cd navx_vmxpi && $(MAKE) run && cd ..

.PHONY: build clean run