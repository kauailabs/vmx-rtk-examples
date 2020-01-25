#!/bin/bash
# Simple bash script to erase any existing vmxrtk ldconfig configuration file,
# and place a new one
sudo rm -r -f /etc/ld.so.conf.d/vmxrtk.conf
sudo cp ./vmxrtk.conf /etc/ld.so.conf.d
sudo ldconfig
