#include <cscore.h>
#include <opencv2/core/core.hpp>
#include <opencv2/videoio/videoio.hpp>
#include <iostream>

using namespace cs;
using namespace cv;

int main() {
	UsbCamera camera("usbcam", 0);
	camera.SetVideoMode(VideoMode::PixelFormat::kMJPEG, 320, 240, 15);
	MjpegServer mjpegServer("httpserver", 8081);
	mjpegServer.SetSource(camera);
	CvSink cvsink("cvsink");
	cvsink.SetSource(camera);
	CvSource cvsource("cvsource",
			VideoMode::PixelFormat::kMJPEG, 320, 240, 15);
	MjpegServer cvMjpegServer("cvhttpserver", 8082);
	cvMjpegServer.SetSource(cvsource);

	Size frameSize(320, 240);
	VideoWriter videoWriter("/data/output.avi",
			VideoWriter::fourcc('F', 'M', 'P', '4'), 15.0, frameSize, true);

	Mat frame;

	int count = 0;
	while (count < 100) {
		uint64_t time = cvsink.GrabFrame(frame);
		printf("Frame Time:  %lld\n", time);
		videoWriter.write(frame);
		count++;
	}
}
