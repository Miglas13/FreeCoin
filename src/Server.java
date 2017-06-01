import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.UUID;
import java.sql.PreparedStatement;


public class Server{

    static final String dbName = "/home/frederico/IdeaProjects/FreeCoin/src/frbased.db";

    public String serverRequest = "none";
    private Socket socket = null;
    private int counter = 0;
    byte[] signature;
    static String bitNumber;
    private final List<Server> killList;
    private final List<Server> serverActions;
    public final static int SOCKET_PORT = 13267;
    public final static int SOCKET_PORT_BROADCAST = 13268;
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

    private static Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:src/frbased.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static int numUtilizadores(){
        String sql = "Select count(*) from user";
        Connection connection = connect();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.getInt(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String bitRandom(int x){
        Random rg = new Random();
        int n = rg.nextInt(x);
        System.out.println(n);
        return Integer.toBinaryString(n);
    }

    //return -1 caso não consiga vencer o challenge
    //return 1 caso consiga vencer o challenge
    // TODO: 20-05-2017 Substituir o return por uma nova coin dada ao user - Enviar mensagem para o utilizador especifico 
    public static void challengeAnswer(Socket socket) throws IOException {
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
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeInt(-1);
                    return;
                }
            }
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(1);
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
    }

    public static void createDB(String dbLocation) {
        String sql = "CREATE TABLE IF NOT EXISTS user (" +
                "id INTEGER     PRIMARY KEY AUTOINCREMENT" +
                ", username     TEXT NOT NULL UNIQUE" +
                ", pubkey       TEXT NOT NULL" +
                ", coins INTEGER NOT NULL DEFAULT 0" +
                ", pass TEXT NOT NULL" +    //representacao da pass (??? hash da pass + salt)
                ", salt TEXT NOT NULL" +
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



    public static void insert(String name, String pass, String salt, String pub) {
        String sql = "INSERT INTO user(username,pass,salt,pubkey) VALUES(?,?,?,?)";

        try (Connection conn = connect()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, pass);
                pstmt.setString(3, salt);
                pstmt.setString(4, pub);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void updatebd(String sql){
        try (Connection conn = connect()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        createDB(Server.dbName);
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(SOCKET_PORT_BROADCAST );
            while (true) {
                Socket connected = socket.accept();
                System.out.println("Connecting...");
                Timer timer = new Timer();
                timer.schedule(new Challenge(connected),0,30000);       //Increasing for testing. Final Version needs to be rescaled to 30000
                challengeAnswer(connected);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    public static String getHash(String pessoa){

        String sql = "SELECT pass FROM user where username = ? ;";

        String hash="";

        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            // set the value
            pstmt.setString(1,pessoa);
            //
            ResultSet rs  = pstmt.executeQuery();

            // loop through the result set
            while (rs.next()) {
                hash = rs.getString("pass");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return hash;

    }

}