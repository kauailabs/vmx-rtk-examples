// This example illustrates how C++ classes can be used from Java using SWIG.
// The Java class gets mapped onto the C++ class and behaves as if it is a Java class.

package navx_vmxpi;

import com.kauailabs.vmx.VMXPi;

public class navx_vmxpi {
  static {
    try {
        System.loadLibrary("vmxpi_hal_java");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load. See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\n" + e);
      System.exit(1);
    }
  }

  public static void main(String argv[]) 
  {
	VMXPi vmx = new VMXPi(false, (byte)50);
	if (vmx.IsOpen()) {
		String v = vmx.getVersion().GetFirmwareVersion();
		System.out.println("Firmware Version:  " + v);
	
		// Delay 25 milliseconds, waiting for first AHRS data sample to be acquired
		vmx.getTime().DelayMilliseconds(25);
	
		for (int i = 0; i < 100; i++) {
			System.out.println(
				"Yaw:  " + vmx.getAHRS().GetYaw() + 
				" Pitch:  " + vmx.getAHRS().GetPitch() + 
				" Roll:  " + vmx.getAHRS().GetRoll() +
				" Timestamp:  " + vmx.getAHRS().GetLastSensorTimestamp());
			vmx.getTime().DelayMilliseconds(20);
		}
	} else {
		System.out.println("Error:  Unable to open VMX Client.");
		System.out.println("");
		System.out.println("        - Is pigpio (or the system resources it requires) in use by another process?");
		System.out.println("        - Does this application have root privileges?");
	}
	vmx.delete(); // Immediately dispose of all resources used by vmx object.
  }
}
