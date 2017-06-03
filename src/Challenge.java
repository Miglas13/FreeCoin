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
    public int s = 1;

    public void run(){
        int x = Server.numUtilizadores();
        if (x==-1)  s = 1;
        else{
            if ((x/2)%2==0) {
                s = (x / 2);
            }
        }
        Server.bitNumber = Server.bitRandom(s);
        int number = Integer.parseInt(Server.bitNumber);
        byte[] message = Integer.toBinaryString(number).getBytes();
        //Escreve isto apenas para teste. Deve ser removido no fim para Server.bitNumber.getBytes();
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(connected.getOutputStream());
            dataOutputStream.writeInt(message.length);
            dataOutputStream.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
