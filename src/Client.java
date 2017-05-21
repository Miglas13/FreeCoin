import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by andremigueldasilvapinho on 21-05-2017.
 */

public class Client {
    public final static int SOCKET_PORT = 13267;
    public final static String SERVER = "127.0.0.1";
    public static void main(String[] args){
        Socket socket = null;
        try {
            socket = new Socket(SERVER,SOCKET_PORT);
            System.out.println("Connecting...");
            while (true){
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                int length = dataInputStream.readInt();
                if (length>0) {
                    byte[] message = new byte[length];
                    dataInputStream.readFully(message, 0, message.length);
                    System.out.println(message.toString());
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
