package full_processing;

import java.math.BigInteger;

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

import com.kauailabs.vmx.VMXPi;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;

import edu.wpi.first.wpilibj.vision.VisionPipeline;

public class full_processing{

	static {
		try {
			System.loadLibrary("vmxpi_hal_java");
		} catch (UnsatisfiedLinkError e) {
			System.err.println("Native code library failed to load. See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\n" + e);
      			System.exit(1);
    		}
  	}

	public static void main(String argv[]) {
		
		/* Instantiate pipeline exported from GRIP.  If not using grip, set pipeline = null; */
		VisionPipeline pipeline = new GripPipeline();

		String outputVideoFilePath = "/data/output.avi"; /* Set to null if video writing not desired */

		/* Open communication to VMX-pi, to acquire IMU data */
		VMXPi vmx = new VMXPi(false, (byte)50);
		if (!vmx.IsOpen()) {
			System.out.println("Error:  Unable to open VMX Client.");
			System.out.println("");
			System.out.println("        - Is pigpio (or the system resources it requires) in use by another process?");
			System.out.println("        - Does this application have root privileges?");
			vmx.delete();  // Immediately dispose of all resources used by vmx object.
			System.exit(1);
		}

		/* Connect NetworkTables */
		/* Note:  actual IP address should be robot IP address */
		NetworkTableInstance inst = NetworkTableInstance.getDefault();
		inst.startClient("192.168.0.113");

		/* Open connection to USB Camera (video device 0 [/dev/video0]) */
		UsbCamera camera = new UsbCamera("usbcam", 0);

		/* Configure Camera */
		/* Note:  Higher resolution & framerate is possible, depending upon processing cpu usage */
		int width = 320;
		int height = 240;
		int frames_per_sec = 15;
		camera.setVideoMode(VideoMode.PixelFormat.kMJPEG, width, height, frames_per_sec);
		
		/* Start raw Video Streaming Server */
		MjpegServer rawVideoServer = new MjpegServer("raw_video_server", 8081);
		rawVideoServer.setSource(camera);
		CvSink cvsink = new CvSink("cvsink");
		cvsink.setSource(camera);

		/* Start processed Video server */
		CvSource cvsource = new CvSource("cvsource",
				VideoMode.PixelFormat.kMJPEG, width, height, frames_per_sec);
		MjpegServer processedVideoServer = new MjpegServer("processed_video_server", 8082);
		processedVideoServer.setSource(cvsource);

		/* Create Video Writer, if enabled */
		Size frameSize = new Size(width, height);
		VideoWriter videoWriter = null;
		if (outputVideoFilePath != null) {
			videoWriter = new VideoWriter(outputVideoFilePath,
				VideoWriter.fourcc('F', 'M', 'P', '4'), (double)frames_per_sec, frameSize, true);
		}

		/* Pre-allocate a video frame */
		Mat frame = new Mat();

		int count = 0;
		while (count < 100) {

			/* Acquire new video frame */
			String videoTimestampString = null;
			long video_timestamp = cvsink.grabFrame(frame);
			if (video_timestamp == 0) {
				cvsink.getError();
				try {
					Thread.sleep((1000/frames_per_sec)/2, 0);
				} catch (InterruptedException e) {
					break;
    				}
				continue;
			} else {
				videoTimestampString = BigInteger.valueOf(video_timestamp).toString();
				System.out.println("Video Timestamp:  " + videoTimestampString);
			}			

			/* Overlay timestamps & orientation data onto video */
		        Imgproc.putText (
		           frame,                          // Video frame
		           videoTimestampString,       	   // Text to be added
		           new Point(30, 30),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);
			String imuTimestampString = Integer.toString(vmx.getAHRS().GetLastSensorTimestamp());
		        Imgproc.putText (
		           frame,                          // Video frame
		           imuTimestampString,        	   // Text to be added
		           new Point(30, 50),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);
			String yawString = Double.toString(vmx.getAHRS().GetYaw());
		        Imgproc.putText (
		           frame,                          // Video frame
		           yawString,          	   	   // Text to be added
		           new Point(30, 70),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);
			String pitchString = Double.toString(vmx.getAHRS().GetPitch());
		        Imgproc.putText (
		           frame,                          // Video frame
		           pitchString,          	   // Text to be added
		           new Point(30, 90),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);
			String rollString = Double.toString(vmx.getAHRS().GetRoll());
		        Imgproc.putText (
		           frame,                          // Video frame
		           rollString,          	   // Text to be added
		           new Point(30, 110),             // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);

			/* Update Network Tables with timestamps & orientation data */
			inst.getEntry("/vmx/videoOSTimestamp").setNumber(BigInteger.valueOf(video_timestamp));
			inst.getEntry("/vmx/navxSensorTimestamp").setNumber(vmx.getAHRS().GetLastSensorTimestamp());
			inst.getEntry("/vmx/navxYaw").setNumber(vmx.getAHRS().GetYaw());
			inst.getEntry("/vmx/navxPitch").setNumber(vmx.getAHRS().GetPitch());
			inst.getEntry("/vmx/navxRoll").setNumber(vmx.getAHRS().GetRoll());

			/* Invoke processing pipeline, if one is present */
			if (pipeline != null) {
				pipeline.process(frame);
			}

			/* Write Frame to video */
			if (videoWriter != null) {
				videoWriter.write(frame);
			}

			count++;
		}
		vmx.delete(); // Immediately dispose of all resources used by vmx object.
	}
}
