package nt_client;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;

public class nt_client {

  public static void main(String argv[]) 
  {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();

    inst.startClient("localhost", 1735);

    while (true) {
      NetworkTableEntry entry = inst.getEntry("/Value");
      if (entry.exists()) {
        System.out.println(entry.getString("not found"));
        break;
      }
    }
    return;
  }

}