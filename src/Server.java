import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.UUID;

public class Server{

    static final String dbName = "/home/frederico/frbased.db";

    public String serverRequest = "none";
    private Socket socket = null;
    private int counter = 0;
    byte[] signature;
    static String bitNumber;
    private final List<Server> killList;
    private final List<Server> serverActions;
    public final static int SOCKET_PORT = 13267;
    public final static String SERVER = "127.0.0.1";  
    public final static String FILE_TO_RECEIVED = "src/downloaded.txt";    // TODO: 20-05-2017  tem de se alterar isto para correr em windows também

    public final static int FILE_SIZE = 6022386;    // TODO: 20-05-2017 verificar se este tamanho está correcto
    public UUID serverThreadID;
    private int maxLocalRetries;

    public Server(Socket socket, List<Server> killList, List<Server> serverActions){
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

    static String hextoBin(String s){
        return new BigInteger(s,16).toString(2);
    }

    public static String bitRandom(){
        Random rg = new Random();
        int n = rg.nextInt(10);
        System.out.println(n);
        return Integer.toBinaryString(n);
    }

    public void sendChallenge(){

    }

    //return -1 caso não consiga vencer o challenge
    //return 1 caso consiga vencer o challenge
    // TODO: 20-05-2017 Substituir o return por uma nova coin dada ao user - Enviar mensagem para o utilizador especifico 
    public static int challengeAnswer(Socket socket) throws IOException {
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try{
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = new FileOutputStream(FILE_TO_RECEIVED);
            byte[] bytes = new byte[16*1024];
            int count;
            count = inputStream.read(bytes);
            System.out.println("TOPKEK!");
            System.out.println(count);
            System.out.println(bytes.toString());
            outputStream.write(bytes,0,count);
            System.out.println("Received File!");
            System.out.println("Checking");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(FILE_TO_RECEIVED);
            byte[] dataBytes = new byte[FILE_SIZE];
            int nread = 0;
            while ((nread = fis.read(dataBytes))!=-1){
                md.update(dataBytes,0,nread);
            }
            byte[] mdBytes = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i=0; i < mdBytes.length; i++){
                sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            String binary = hextoBin(sb.toString());
            System.out.println(binary);
            System.out.println(bitNumber);
            for (int i=0; i < bitNumber.length(); i++){
                if (bitNumber.charAt(i) == binary.charAt(i)){
                    continue;
                }else {
                    return -1;
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (fos!=null)  fos.close();
            if (bos!=null)  bos.close();
            if (socket!=null)   socket.close();
        }
        return 1;       // TODO: 30-05-2017 Fred: adiciona 1 coin à BD 
    }

    public static void createDB(String dbLocation) {
        String sql = "CREATE TABLE IF NOT EXISTS user (" +
                "id INTEGER     PRIMARY KEY AUTOINCREMENT" +
                ", username     TEXT NOT NULL UNIQUE" +
                ", pubkey       TEXT NOT NULL" +
                ", coins INTEGER NOT NULL DEFAULT 0" +
                ", pass TEXT NOT NULL" +    //representacao da pass (??? hash da pass + salt)
                ")";

        String sql1 = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", PK_Emissor   TEXT NOT NULL" +
                ", PK_Receptor  TEXT NOT NULL" +
                ", coins    INTEGER NOT NULL" +
                ")";

        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbLocation);
                Statement statement = connection.createStatement();
        ) {
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.execute(sql);
            statement.execute(sql1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        createDB(Server.dbName);
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(SOCKET_PORT);
            while (true) {
                Socket connected = socket.accept();
                System.out.println("Connecting...");
                Timer timer = new Timer();
                timer.schedule(new Challenge(connected),0,999999999);       //Increasing for testing. Final Version needs to be rescaled to 30000
                int i = challengeAnswer(connected);
                System.out.println(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}