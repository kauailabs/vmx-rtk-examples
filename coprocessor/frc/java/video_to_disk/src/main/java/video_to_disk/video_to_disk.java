package video_to_disk;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

public class video_to_disk{

	public static void main(String argv[]) {
		UsbCamera camera = new UsbCamera("usbcam", 0);
		camera.setVideoMode(VideoMode.PixelFormat.kMJPEG, 320, 240, 15);
		MjpegServer mjpegServer = new MjpegServer("httpserver", 8081);
		mjpegServer.setSource(camera);
		CvSink cvsink = new CvSink("cvsink");
		cvsink.setSource(camera);
		CvSource cvsource = new CvSource("cvsource",
				VideoMode.PixelFormat.kMJPEG, 320, 240, 15);
		MjpegServer cvMjpegServer = new MjpegServer("cvhttpserver", 8082);
		cvMjpegServer.setSource(cvsource);

		Size frameSize = new Size(320, 240);
		VideoWriter videoWriter = new VideoWriter("/media/pi/data/output.avi",
				VideoWriter.fourcc('F', 'M', 'P', '4'), 15.0, frameSize, true);

		Mat frame = new Mat();

		int count = 0;
		while (count < 100) {
			long time = cvsink.grabFrame(frame);

			videoWriter.write(frame);
			count++;
		}
	}
}

// final fourCC = new int("XVID");
// VideoWriter out = new VideoWriter("output.avi", fourCC, 20.0,());