import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothPIServer {
    java.util.UUID serviceUUid = java.util.UUID.fromString("b03e9d9c-ff1e-11e4-a322-1697f925ec7b");
    UUID uuid = new UUID(serviceUUid.toString().replace("-", ""), false);
	StreamConnectionNotifier service;

    public void startserver() {
        try {
            String url = "btspp://localhost:" + uuid +
                    ";name=remoteNotifier;authenticate=false;authorize=false;encrypt=false";
            if(service == null)
				service = (StreamConnectionNotifier) Connector.open(url);

            System.out.println("waiting to accept");
            StreamConnection con = service.acceptAndOpen();
            OutputStream out = con.openOutputStream();
            InputStream in = con.openInputStream();
            System.out.println("Accepted");

            byte[] buffer = new byte[1024];
			buffer[0] = 'A';
            out.write(buffer);
            int bytes_read = in.read(buffer);
            String received = new String(buffer, 0, bytes_read, Charset.forName("utf-8"));
            System.out.println
                    ("Message:" + received);
            in.close();
			out.close();
			con.close();
        } catch (IOException e) {
            System.err.print(e.toString());
        }
    }

    public static void main(String args[]) {
        try {
            LocalDevice local = LocalDevice.getLocalDevice();
            System.out.println("Server Started:\n"
                    + local.getBluetoothAddress()
                    + "\n" + local.getFriendlyName());

            BluetoothPIServer ff = new BluetoothPIServer();
            while (true) {
                ff.startserver();
            }
        }
        catch (Exception e) {
            System.err.print(e.toString());
        }
    }
}