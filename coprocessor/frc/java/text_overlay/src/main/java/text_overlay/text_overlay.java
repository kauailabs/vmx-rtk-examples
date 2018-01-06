package text_overlay;

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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.math.BigInteger;

public class text_overlay{

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
		VideoWriter videoWriter = new VideoWriter("/data/output.avi",
				VideoWriter.fourcc('F', 'M', 'P', '4'), 15.0, frameSize, true);

		Mat frame = new Mat();

		int count = 0;
		while (count < 100) {
			long time = cvsink.grabFrame(frame);

			// overlay time onto video
			String textString = BigInteger.valueOf(time).toString();
		        // Draw Text to Image
		        Imgproc.putText (
		           frame,                          // Matrix obj of the image
		           textString,          	   // Text to be added
		           new Point(30, 30),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);

			videoWriter.write(frame);
			count++;
		}
	}
}

// final fourCC = new int("XVID");
// VideoWriter out = new VideoWriter("output.avi", fourCC, 20.0,());