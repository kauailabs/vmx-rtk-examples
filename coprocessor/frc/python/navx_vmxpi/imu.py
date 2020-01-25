from importlib.machinery import SourceFileLoader
import sys
from time import sleep

sys.path.append('/usr/local/lib/vmxpi/')
vmxpi = SourceFileLoader('vmxpi_hal_python', '/usr/local/lib/vmxpi/vmxpi_hal_python.py').load_module()

vmx = vmxpi.VMXPi(False,50)
if vmx.IsOpen():
	v = vmx.getVersion().GetFirmwareVersion()
	print("Firmware Version:  ", v)
	# Delay 25 milliseconds, waiting for first AHRS data sample to be acquired
	vmx.getTime().DelayMilliseconds(25);
	for i in range (1,100):
		print ("Yaw:  %.2f Pitch:  %.2f Roll:  %.2f Timstamp: %d" % (vmx.getAHRS().GetYaw(), vmx.getAHRS().GetPitch(), vmx.getAHRS().GetRoll(), vmx.getAHRS().GetLastSensorTimestamp()))
		vmx.getTime().DelayMilliseconds(20)
else:
	print ("Error:  Unable to open VMX Client.")
	print ("")
	print ("        - Is pigpio (or the system resources it requires) in use by another process?")
	print ("        - Does this application have root privileges?")
