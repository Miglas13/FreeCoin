import com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetException;
import com.sun.xml.internal.ws.api.message.Message;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

public class ServerThread extends Thread{

    static final String dbName = "/home/andremigueldasilvapinho/frbased.db";

    public String serverRequest = "none";
    private Socket socket = null;
    private int counter = 0;
    byte[] signature;
    static String bitNumber;
    private final List<ServerThread> killList;
    private final List<ServerThread> serverActions;
    public final static int SOCKET_PORT = 13267;
    public final static String SERVER = "127.0.0.1";  
    public final static String FILE_TO_RECEIVED = "~/Trasnferências/downloaded.txt";    // TODO: 20-05-2017  tem de se alterar isto para correr em windows também

    public final static int FILE_SIZE = 6022386;    // TODO: 20-05-2017 verificar se este tamanho está correcto
    public UUID serverThreadID;
    private int maxLocalRetries;

    public ServerThread(Socket socket, List<ServerThread> killList, List<ServerThread> serverActions){
        super("ServerThread");
        this.socket = socket;
        this.killList = killList;
        this.serverActions = serverActions;
    }



    //Todo

    //// TODO: 18/05/2017 Fred fica com as primeiras duas funcionalidades basicas e curvas elipticas
    //// TODO: 18/05/2017 Rui faz 4ª simples e 1ª avançada
    //// TODO: 18/05/2017 Barbara 4ªavançada
    //// TODO: 18/05/2017 André 5ªsimples e 3ªavançada

    //// TODO: 18/05/2017  Guardar password é efetuada com o calculo do hash 1024 ou 2048 vezes + salt

    // TODO: 17-05-2017 Criar Classe das Message para ver se determinado documento está assinado, etc.
    
    public void send(String msg, byte[] signature){
        // TODO: 17-05-2017 StandBy
    }

    public void sendEncrypted(String msg, String keyIndex){
        String encripted= null;     // TODO: 17-05-2017 Acrescentar a classe do DeterministicDSA e fazer a chamada do encrypt - return encripted
    }

    // TODO: 17-05-2017 Verificar Assinaturas
    // TODO: 17-05-2017 Assinar e enviar 
    // TODO: 17-05-2017 Guardar transações SQL 
    // TODO: 17-05-2017 Criar moedas, tamos pobres 


    // TODO: 17-05-2017 Do lado do utilizador: 
    // TODO: 17-05-2017 Gerar chaves DSA
    // TODO: 17-05-2017 login 
    // TODO: 17-05-2017 só sabe as suas transações 

    public static String bitRandom(){
        Random rg = new Random();
        int n = rg.nextInt(10);
        return Integer.toBinaryString(n);
    }

    static String hextoBin(String s){
        return new BigInteger(s,16).toString(2);
    }


    //return -1 caso não consiga vencer o challenge
    //return 1 caso consiga vencer o challenge
    // TODO: 20-05-2017 Substituir o return por uma nova coin dada ao user - Enviar mensagem para o utilizador especifico 
    public int challenge() throws IOException {
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        Socket socket = null;
        try{
            socket = new Socket(SERVER,SOCKET_PORT);
            System.out.println("Connecting...");
            byte[] mybytearray = new byte[FILE_SIZE];
            InputStream inputStream = socket.getInputStream();
            fos = new FileOutputStream(FILE_TO_RECEIVED);
            bos = new BufferedOutputStream(fos);
            bytesRead = inputStream.read(mybytearray,0,mybytearray.length);
            current = bytesRead;

            do {
                bytesRead = inputStream.read(mybytearray,current,(mybytearray.length-current));
                if (bytesRead >= 0) current+=bytesRead;
            }while (bytesRead>-1);

            bos.write(mybytearray,0,current);
            bos.flush();
            System.out.println("File " + FILE_TO_RECEIVED + " downloaded (" + current + "bytes read)");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos!=null)  fos.close();
            if (bos!=null)  bos.close();
            if (socket!=null)   socket.close();
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(FILE_TO_RECEIVED);
            byte[] dataBytes = new byte[FILE_SIZE];
            int nread = 0;
            while ((nread = fis.read(dataBytes))!=-1){
                md.update(dataBytes,0,nread);
            };
            byte[] mdBytes = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i=0; i < mdBytes.length; i++){
                sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            String binary = hextoBin(sb.toString());
            int flag = 0;
            for (char x : binary.toCharArray()
                 ) {
                for (char y : bitNumber.toCharArray()
                     ) {
                    if (x == y) continue;
                    else{
                        flag = 1;
                        break;
                    }
                }
                if (flag==1) {
                    return -1;
                }
                else continue;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void main(String[] args) throws IOException{
        bitNumber = bitRandom();
    }
}