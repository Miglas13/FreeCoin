import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;

/**
 * Created by andremigueldasilvapinho on 21-05-2017.
 */
public class Challenge extends TimerTask {
    public String bitNumber = null;
    public Socket connected = null;
    public Challenge(Socket connected){
        this.connected = connected;
    }

    public void run(){
        Server.bitNumber = Server.bitRandom();
        //Escreve isto apenas para teste. Deve ser removido no fim para Server.bitNumber.getBytes();
        byte[] message = Integer.toBinaryString(10).getBytes();
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(connected.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeInt(message.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
