#!/usr/bin/env python3

import sys
from importlib.machinery import SourceFileLoader

sys.path.append('/home/pi/.local/lib/python3.7/site-packages')

from networktables import NetworkTables
from networktables.util import ntproperty
#import ntcore as nt
import cscore as cs
import cv2 as cv
import numpy as np

from time import sleep
from sys import exit

sys.path.append('/usr/local/lib/vmxpi/')
vmxpi = SourceFileLoader('vmxpi_hal_python', '/usr/local/lib/vmxpi/vmxpi_hal_python.py').load_module()

vmx = vmxpi.VMXPi(False,50)
if vmx.IsOpen() is False:
	print("Error:  Unable to open VMX Client.")
	print("")
	print("        - Is pigpio (or the system resources it requires) in use by another process?")
	print("        - Does this application have root privileges?")
	sys.exit(0)

# Instantiate pipeline exported from GRIP.  If not using grip, set pipeline = None;
pipeline = None

# Video output file.  Set to None if video writing not desired.
outputVideoFilePath = "/media/pi/data/output.avi"
videoWriter = None

# Connect NetworkTables
# Note:  actual IP address should be robot IP address
NetworkTables.initialize(server="192.168.0.113")

width=320
height=240
frames_per_sec=15

# Open connection to USB Camera (video device 0 [/dev/video0]) */
camera = cs.UsbCamera("usbcam", 0)
camera.setVideoMode(cs.VideoMode.PixelFormat.kMJPEG, width, height, frames_per_sec)

# Start raw Video Streaming Server
mjpegServer = cs.MjpegServer("httpserver", 8081)
mjpegServer.setSource(camera)

cvsink = cs.CvSink("cvsink")
cvsink.setSource(camera)

# Start processed Video server
cvsource = cs.CvSource("cvsource", cs.VideoMode.PixelFormat.kMJPEG, width, height, frames_per_sec)

cvMjpegServer = cs.MjpegServer("cvhttpserver", 8082)
cvMjpegServer.setSource(cvsource)

# Create Video Writer, if enabled
if outputVideoFilePath is not None:
	frameSize = (width, height)
	videoWriter = cv.VideoWriter(outputVideoFilePath,
				cv.VideoWriter.fourcc('M', 'J', 'P', 'G'), 15.0, frameSize, True)

img = np.zeros(shape=(height, width, 3), dtype=np.uint8)    

ntVideoOsTimestamp = ntproperty('/vmx/videoOSTimestamp', 0)
ntNavxSensorTimestamp = ntproperty('/vmx/navxSensorTimestamp', 0)
ntNavxYaw = ntproperty('/vmx/navxYaw', 0)
ntNavxPitch = ntproperty('/vmx/navxPitch', 0)
ntNavxRoll = ntproperty('/vmx/navxRoll', 0)

count = 0
while count < 100:
	video_timestamp, img = cvsink.grabFrame(img)
	if video_timestamp == 0:
		print("error:", cvsink.getError())
		sleep (float(frames_per_sec *2) / 1000.0)
		count = count + 1
		continue

	print("Timestamp: %d" % video_timestamp)

	videoTimestampString = 'video: %d' % video_timestamp
	cv.putText(img, videoTimestampString, (30,30), cv.FONT_HERSHEY_SIMPLEX,0.5,(255,255,255),1,cv.LINE_AA)
	navXSensorTimestampString = 'navX:  %d' % vmx.getAHRS().GetLastSensorTimestamp()
	cv.putText(img, navXSensorTimestampString, (30,50), cv.FONT_HERSHEY_SIMPLEX,0.5,(255,255,255),1,cv.LINE_AA)
	yawString = 'Yaw:  %.2f' % vmx.getAHRS().GetYaw()
	cv.putText (img, yawString, (30,70), cv.FONT_HERSHEY_SIMPLEX,0.5,(255,255,255),1,cv.LINE_AA)
	pitchString = 'Pitch: %.2f' % vmx.getAHRS().GetPitch()
	cv.putText (img, pitchString, (30,90), cv.FONT_HERSHEY_SIMPLEX,0.5,(255,255,255),1,cv.LINE_AA)
	rollString = 'Roll:  %.2f' % vmx.getAHRS().GetRoll()
	cv.putText (img, rollString, (30,110), cv.FONT_HERSHEY_SIMPLEX,0.5,(255,255,255),1,cv.LINE_AA)

	# Update Network Tables with timestamps & orientation data
	ntVideoOsTimestamp = video_timestamp;
	ntNavxSensorTimestamp = vmx.getAHRS().GetLastSensorTimestamp();
	ntNavxYaw = vmx.getAHRS().GetYaw();
	ntNavXPitch = vmx.getAHRS().GetPitch();
	ntNavXRoll = vmx.getAHRS().GetRoll();

	# Invoke processing pipeline, if one is present
	if pipeline is not None:
		pipeline.process(frame)

	# Write Frame to video file
	if videoWriter is not None:
		videoWriter.write(img)
	
	count = count + 1
