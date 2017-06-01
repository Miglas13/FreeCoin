import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
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
    public final static int SOCKET_PORT_BRPOADCAST = 13268;
    public final static String SERVER = "127.0.0.1";
    public final static String FILE_TO_SEND = "src/text.txt";
    public static String nome = null;

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
            byte[] bytes1 = new byte[10];
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int number = dataInputStream.readInt();
            if (number == 1){
                System.out.println("Boa! Conseguiste uma coin!");
                String sql = "UPDATE user" +
                        "SET freecoin = " +
                        "(SELECT freecoin FROM user WHERE nome = " +
                        nome + ")" +
                        " + 1";
                // TODO: 01-06-2017 Enviar sql para o Server e inserir na BD
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*

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
    */

    private static void challenge(Socket socket){
        DataInputStream dataInputStream = null;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            int length = dataInputStream.readInt();
            if (length>0) {
                byte[] message = new byte[length];
                dataInputStream.readFully(message, 0, message.length);
                String s = new String(message,"US-ASCII");
                System.out.println(s);
                solveChallenge(Integer.parseInt(s),socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        //intro();
        Socket socket = null;
        try {
            socket = new Socket(SERVER,SOCKET_PORT_BRPOADCAST);
            System.out.println("Connecting...");
            System.out.println("Fazer challenge? y/n");
            Scanner scanner = new Scanner(System.in);
            String s = scanner.nextLine();
            if (s.equals("y")){
                challenge(socket);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        nome = sc.nextLine();

        System.out.println("Insira uma password:\n");
        String pass = sc.nextLine();

        String pubkey = "";

        //gerar salt

        System.err.println(getNextSalt());

        String sal = getNextSalt().toString();

        //hash da passe - feito

        String salepassword = pass+sal;

        byte[] digest = null;

        for (int i = 0; i < 2048; i++) {



            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            try {
                md.update(salepassword.getBytes("UTF-8")); // Change this to "UTF-16" if needed
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            digest = md.digest();
            salepassword  = String.format("%064x", new java.math.BigInteger(1, digest));

        }

        pass = String.format("%064x", new java.math.BigInteger(1, digest));

        //gerar chaves - feito

        PrivateKey priv=null;

        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            KeyPair pair = keyGen.generateKeyPair();
            priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();
            pubkey = pub.toString();
            System.out.println(pub.toString());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //guardar a chave no pc do user
        String filename = nome+".pk";
        try (PrintStream out = new PrintStream(new FileOutputStream(filename))) {
            out.print(priv.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //inserir - feito

        //insert(nome, pass, sal, pubkey);
        Server.insert(nome, pass, sal, pubkey);


    }

    public static byte[] getNextSalt() {

        byte[] salt = new byte[32];

        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(salt);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return salt;

    }


    /*

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
    }*/


    //TODO aranjar a geração do salt


}
