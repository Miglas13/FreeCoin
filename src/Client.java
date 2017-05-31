import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by andremigueldasilvapinho on 21-05-2017.
 */

public class Client {
    public final static int SOCKET_PORT = 13267;
    public final static String SERVER = "127.0.0.1";
    public final static String FILE_TO_SEND = "src/text.txt";

    public static void solveChallenge(int binary, Socket socket){
        try {
            File file = new File(FILE_TO_SEND);
            long length = file.length();
            byte[] bytes = new byte[16*1024];
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = socket.getOutputStream();
            int count;
            while ((count = inputStream.read(bytes))>0){
                outputStream.write(bytes,0,count);
            }
            System.out.println("File sent!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:/home/frederico/frbased.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void main(String[] args){
        Socket socket = null;
        try {
            socket = new Socket(SERVER,SOCKET_PORT);
            System.out.println("Connecting...");
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                int length = dataInputStream.readInt();
                if (length>0) {
                    byte[] message = new byte[length];
                    dataInputStream.readFully(message, 0, message.length);
                    String s = new String(message,"US-ASCII");
                    System.out.println(s);
                    solveChallenge(Integer.parseInt(s),socket);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        intro();


    }


    public static void intro(){
        System.out.println("Bem-vindo à aplicação, o que deseja fazer?\n\n1 - Login:\n2 - Registo:\n\n");

        Scanner sc = new Scanner(System.in);
        int opt = sc.nextInt();

        switch (opt) {
            case 1:
                System.out.println("Login:");
                break;
            case 2:
                Registo();
                break;
        }

    }

    public static void Registo(){

        System.out.println("Insira um nome para Login:\n");
        Scanner sc = new Scanner(System.in);
        String nome = sc.nextLine();

        System.out.println("Insira uma password:\n");
        String pass = sc.nextLine();

        String pubkey = "";

        //hash da passe

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            md.update(pass.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] digest = md.digest();

        pass = String.format("%064x", new java.math.BigInteger(1, digest));

        //gerar salt

        String salt = "salt";

        //gerar chaves


        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();
            pubkey = pub.toString();
            System.out.println(pub.toString());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        //inserir
        insert(nome, pass, salt, pubkey);

    }

    public static void insert(String name, String pass, String salt, String pub) {
        String sql = "INSERT INTO user(username,pass,salt,pubkey) VALUES(?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, pass);
            pstmt.setString(3, salt);
            pstmt.setString(4, pub);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
