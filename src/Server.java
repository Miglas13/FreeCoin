import sun.misc.IOUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    public final static String FILE_TO_RECEIVED = "src/downloaded.txt";

    public final static int FILE_SIZE = 6022386;
    public UUID serverThreadID;
    private int maxLocalRetries;

    public Server(Socket socket, List<Server> killList, List<Server> serverActions){
        this.socket = socket;
        this.killList = killList;
        this.serverActions = serverActions;
    }
    public void sendEncrypted(String msg, String keyIndex){
        String encripted= null;
    }

    static String hextoBin(String s){
        return new BigInteger(s,16).toString(2);
    }

    private static Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:/home/frederico/IdeaProjects/FreeCoin/src/frbased.db";
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
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getPubKey(String user){
        String sql = "Select pubkey from user where username = ?";
        Connection connection = connect();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,user);
            ResultSet rs = preparedStatement.executeQuery();
            String pubKey=rs.getString(1);
            preparedStatement.close();
            rs.close();
            return pubKey;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Not exists in database";
    }

    public static List<String> getMyTransactions(String user){
        System.out.println(user);

        List<String> transacao= new ArrayList<String>();
        Connection connection = connect();
        String sql2= "Select * from transactions where username = ? ;";
        try {

            PreparedStatement preparedStatement= connection.prepareStatement(sql2);
            preparedStatement.setString(1, user);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                transacao.add(rs.getString(2) + ";" + rs.getString(3)+";");

            }
            preparedStatement.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Erro!!! Não tem transações");

        }
        return transacao;
    }

    public static List<String> getAllTransactions(){

        List<String> transacao= new ArrayList<String>();
        Connection connection = connect();
        try {
            String sql2= "Select * from transations ";
            PreparedStatement preparedStatement = connection.prepareStatement(sql2);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                transacao.add(rs.getInt(0) +";" + rs.getString(1) + ";" + rs.getString(2)+";"+
                        rs.getInt(3) + ";" + rs.getDate(4));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transacao;
    }


    public static void signAvancadaServer(String in, String siga) throws FileNotFoundException {
        try {
            File out= new File(in);
            File sig = new File(siga);
            FileOutputStream sign = new FileOutputStream(sig);
            PrivateKey privServer = null;
            String pubkeyServer = "";

            KeyPairGenerator keyGen = null;

            keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            KeyPair pair = keyGen.generateKeyPair();
            privServer = pair.getPrivate();

            PublicKey pub = pair.getPublic();
            pubkeyServer = pub.toString();

            //System.out.println(pub.toString());
            Signature dsa = Signature.getInstance("SHA1withECDSA");
            dsa.initSign(privServer);
            FileInputStream fis = new FileInputStream(out);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            }

            bufin.close();
            byte[] realSig = dsa.sign();

            // Save signature
            sign.write(realSig);
            sign.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public  static int getMyCoins(String user){


        String sql = "SELECT coins FROM user where username = ? ;";

        int coin=0;

        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            // set the value
            pstmt.setString(1,user);
            //
            ResultSet rs  = pstmt.executeQuery();
            pstmt.close();


            // loop through the result set
            while (rs.next()) {
                coin = rs.getInt("coins");
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return coin;





    }

    public static int verifyTransaction(String nomeFich, String PK, String PKD, int montante, String destinatario){
         final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");



        LocalDate localDate = LocalDate.now();
        System.out.println(DateTimeFormatter.ofPattern("yyy/MM/dd").format(localDate).toString());
        String data= DateTimeFormatter.ofPattern("yyy/MM/dd").format(localDate).toString();
        try
        {


            Connection connection = connect();
            try {
                String sql2= "Select count(*) from user where pubkey = ? ";
                String sql10 ="Select username from user where pubkey = ?";
                String sql3= "Select count(*) from user where pubkey = ?";
                String sql4= "Select coins from user where pubkey = ?";
                String sql6= "Select coins from user where pubkey = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql2);
                preparedStatement.setString(1,PK);
                ResultSet rs = preparedStatement.executeQuery();
                System.out.println(rs.getInt(1));
                if(rs.getInt(1) ==1){
                    preparedStatement=connection.prepareStatement(sql3);
                    preparedStatement.setString(1,PKD);
                    rs=preparedStatement.executeQuery();
                    System.out.println(rs.getInt(1));
                    if (rs.getInt(1)==1){
                        preparedStatement=connection.prepareStatement(sql4);
                        preparedStatement.setString(1,PK);
                        rs=preparedStatement.executeQuery();
                        int coinsEmissor= rs.getInt(1);
                        System.out.println(coinsEmissor);
                        if (coinsEmissor>= montante){
                            signAvancadaServer(nomeFich, "Server" + nomeFich);
                            preparedStatement=connection.prepareStatement(sql10);
                            preparedStatement.setString(1,PK);
                            rs=preparedStatement.executeQuery();
                            String user=rs.getString(1);
                            System.out.println(user + "aqui");
                            preparedStatement.close();
                            rs.close();
                            String sql5= "Update user set coins = ? where username = ?;" ;
                            preparedStatement=connection.prepareStatement(sql5);
                            preparedStatement.setInt(1,(coinsEmissor-montante));
                            System.out.println((coinsEmissor-montante));
                            preparedStatement.setString(2,user);
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                            preparedStatement=connection.prepareStatement(sql6);
                            preparedStatement.setString(1,PKD);
                            rs=preparedStatement.executeQuery();
                            int coinsDest = rs.getInt(1);
                            String sql7= "Update user set coins = ? where username= ? ;" ;
                            preparedStatement=connection.prepareStatement(sql7);
                            preparedStatement.setInt(1,(coinsDest+montante));
                            System.err.println((coinsDest+montante));
                            preparedStatement.setString(2,destinatario);
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                            String sql8= "Insert into transactions ( username, PK_Emissor, PK_Receptor, coins, Data ) values(?,?,?,?,?)";
                            preparedStatement=connection.prepareStatement(sql8);
                            preparedStatement.setString(1,user);
                            preparedStatement.setString(2,PK);
                            preparedStatement.setString(3,PKD);
                            preparedStatement.setInt(4,montante);
                            preparedStatement.setString(5,data);
                            preparedStatement.executeUpdate();

                            preparedStatement.close();
                            rs.close();
                        }
                        preparedStatement.close();
                        rs.close();

                    }
                    preparedStatement.close();
                    rs.close();


                }
                preparedStatement.close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", nomeFich);
            e.printStackTrace();

        }

        return 0;
    }

    public static String bitRandom(int x){
        Random rg = new Random();
        int n = rg.nextInt(x);
        System.out.println("Number: " + Integer.toBinaryString(n));
        return Integer.toBinaryString(n);
    }


    //return -1 caso não consiga vencer o challenge
    //return 1 caso consiga vencer o challenge
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
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String s = dataInputStream.readUTF();
            if (s.equals("Wrong File")){
                System.out.println("User introduziu mal o ficheiro.\n" +
                        "Terminate --- Kill challenge for user");
                return;
            }else if (s.equals("1")){
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
                ", username TEXT NOT NULL"+
                ", PK_Emissor   TEXT NOT NULL" +
                ", PK_Receptor  TEXT NOT NULL" +
                ", coins    INTEGER NOT NULL" +
                ", Data      Date not null" +
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
                pstmt.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void updatePubKey(String users, String pubKey){


        try
        {

            System.err.println(users);
            System.err.println(pubKey);

            Connection conn=connect();
            // create our java preparedstatement using a sql update query
            String query="UPDATE user SET pubkey = ? WHERE username = ? ;";
            PreparedStatement pstmt = conn.prepareStatement(query);

            // set the preparedstatement parameters
            pstmt.setString(1,pubKey);
            pstmt.setString(2,users);
            pstmt.executeUpdate();
            pstmt.close();


        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }


    public static void updatebd(String nome, int coins){

        Connection conn=connect();

        try {

            // create our java preparedstatement using a sql update query
            String query="UPDATE user SET coins = ? WHERE username = ? ;";

            PreparedStatement pstmt2 = conn.prepareStatement(query);
            // set the preparedstatement parameters
            pstmt2.setInt(1,coins);
            pstmt2.setString(2,nome);
            pstmt2.executeUpdate();
            pstmt2.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


/*
    public static void updatebd(String nome){
        String sql = "SELECT coins FROM user where username = ? ;";
        try (Connection conn = connect()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1,nome);
                ResultSet resultSet = pstmt.executeQuery();
               // resultSet.close();
                pstmt.close();
                int coins = resultSet.getInt("coins");
                String sql1 = "Update user set coins = ? where username = ? ;";
                pstmt = conn.prepareStatement(sql1);
                int newcoins = coins+1;
                pstmt.setInt(1,newcoins); //alterei de coins+1
                pstmt.setString(2,nome);
                pstmt.executeUpdate();
                pstmt.close();
                //resultSet.close();
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

   */

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
                DataInputStream dataInputStream = new DataInputStream(connected.getInputStream());
                String s = dataInputStream.readUTF();
                System.out.println(s);
                if (s.equals("y"))        challengeAnswer(connected);
                else continue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }




    }


    public static byte[] getNounce(){

        byte[] nounce = new byte[16];

        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(nounce);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return nounce;

    }

    public static int getId(){

        int id = 0;

        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            id = random.nextInt();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return id;

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

    public static String getSalt(String pessoa){

        String sql = "SELECT salt FROM user where username = ? ;";

        String hash="";

        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            // set the value
            pstmt.setString(1,pessoa);
            //
            ResultSet rs  = pstmt.executeQuery();

            // loop through the result set
            while (rs.next()) {
                hash = rs.getString("salt");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return hash;

    }

    public static int VerifyDups(String pessoa){

        String sql = "SELECT username FROM user where username = ? ;";
        int verify=0;

        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            // set the value
            pstmt.setString(1,pessoa);
            //
            ResultSet rs  = pstmt.executeQuery();

            // loop through the result set
            if(rs.getString("username").isEmpty()==false) {
                verify = 1;
            }
            else{
                verify = 0;
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return verify;

    }

    public static int VerifyAuth(int id, byte[] nounce, String username, String pw, String lastHash){

        String HashedPassword = getHash(username);

        String toBeHashed = id + nounce.toString() + HashedPassword;

        String HashToVerify = "";
        byte[] digest = null;
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            md.update(toBeHashed.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        digest = md.digest();
        HashToVerify = String.format("%064x", new java.math.BigInteger(1, digest));

        System.err.println("Hash calculado do lado do servidor = " + HashToVerify);

        if(HashToVerify.equals(lastHash)){
            return 1;
        }
        else
            return 0;
    }



}