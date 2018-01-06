package full_processing;

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
		

		VisionPipeline pipeline = new GripPipeline();

		VMXPi vmx = new VMXPi(false, (byte)50);
		if (!vmx.IsOpen()) {
			System.out.println("Error:  Unable to open VMX Client.");
			System.out.println("");
			System.out.println("        - Is pigpio (or the system resources it requires) in use by another process?");
			System.out.println("        - Does this application have root privileges?");
			vmx.delete();  // Immediately dispose of all resources used by vmx object.
			System.exit(1);
		}

		// Connect NetworkTables, and get access to the publishing table
		NetworkTableInstance inst = NetworkTableInstance.getDefault();
		inst.startClient("192.168.0.113");

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
			String osTimeString = BigInteger.valueOf(time).toString();
		        // Draw Text to Image
		        Imgproc.putText (
		           frame,                          // Matrix obj of the image
		           osTimeString,          	   // Text to be added
		           new Point(30, 30),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);
			String imuTimeString = Integer.toString(vmx.getAHRS().GetLastSensorTimestamp());
		        Imgproc.putText (
		           frame,                          // Matrix obj of the image
		           imuTimeString,          	   // Text to be added
		           new Point(30, 50),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);
			String yawString = Double.toString(vmx.getAHRS().GetYaw());
		        Imgproc.putText (
		           frame,                          // Matrix obj of the image
		           yawString,          	   	   // Text to be added
		           new Point(30, 70),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);
			String pitchString = Double.toString(vmx.getAHRS().GetPitch());
		        Imgproc.putText (
		           frame,                          // Matrix obj of the image
		           pitchString,          	   // Text to be added
		           new Point(30, 90),              // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);
			String rollString = Double.toString(vmx.getAHRS().GetRoll());
		        Imgproc.putText (
		           frame,                          // Matrix obj of the image
		           rollString,          	   // Text to be added
		           new Point(30, 110),             // point
		           Core.FONT_HERSHEY_SIMPLEX ,     // front face
		           0.5,                            // front scale
		           new Scalar(255, 255, 255),      // Scalar object for color (RGB)
		           2                               // Thickness
      			);

			inst.getEntry("/vmx/imageOSTimestamp").setNumber(BigInteger.valueOf(time));
			inst.getEntry("/vmx/navxSensorTimestamp").setNumber(vmx.getAHRS().GetLastSensorTimestamp());
			inst.getEntry("/vmx/navxYaw").setNumber(vmx.getAHRS().GetYaw());
			inst.getEntry("/vmx/navxPitch").setNumber(vmx.getAHRS().GetPitch());
			inst.getEntry("/vmx/navxRoll").setNumber(vmx.getAHRS().GetRoll());

			if (pipeline != null) {
				pipeline.process(frame);
			}

			videoWriter.write(frame);
			count++;
		}
		vmx.delete(); // Immediately dispose of all resources used by vmx object.
	}
}

// final fourCC = new int("XVID");
// VideoWriter out = new VideoWriter("output.avi", fourCC, 20.0,());