#!/usr/bin/env python3

import cscore as cs
import cv2 as cv
import numpy as np

width=320
height=240
frames_per_sec=15

camera = cs.UsbCamera("usbcam", 0)
camera.setVideoMode(cs.VideoMode.PixelFormat.kMJPEG, width, height, frames_per_sec)

mjpegServer = cs.MjpegServer("httpserver", 8081)
mjpegServer.setSource(camera)

cvsink = cs.CvSink("cvsink")
cvsink.setSource(camera)

cvsource = cs.CvSource("cvsource", cs.VideoMode.PixelFormat.kMJPEG, width, height, frames_per_sec)

cvMjpegServer = cs.MjpegServer("cvhttpserver", 8082)
cvMjpegServer.setSource(cvsource)

frameSize = (width, height)
videoWriter = cv.VideoWriter("/data/output.avi",
				cv.VideoWriter.fourcc('F', 'M', 'P', '4'), 15.0, frameSize, True)

img = np.zeros(shape=(height, width, 3), dtype=np.uint8)    

count = 0
while count < 100:
	time = cvsink.grabFrame(img)
	print("Timestamp: %d" % time[0])
	videoWriter.write(img)
	count = count + 1
