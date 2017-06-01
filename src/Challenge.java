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
            } // TODO: 01-06-2017 FRED VÊ ESTA MERDA SE FOR Nº IMPAR SE ESTÀ A RETORNAR O QUE ESTAVA ANTES
        }
        Server.bitNumber = Server.bitRandom(s);
        //Escreve isto apenas para teste. Deve ser removido no fim para Server.bitNumber.getBytes();
        byte[] message = Integer.toBinaryString(10).getBytes();
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
