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
    public final static String FILE_TO_SEND = "src/text.txt"; // TODO: 01-06-2017 Mudar para o utilizador enviar o path para o ficheiro que quer enviar 
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
                        "SET coins = " +
                        "(SELECT coins FROM user WHERE username = " +
                        nome + ")" +
                        " + 1"; //falta corrigir o erro de sintaxe do sql
                Server.updatebd(sql);
                // TODO: 01-06-2017 Enviar sql para o Server e inserir na BD
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        intro();

    }


    public static void challenge(){

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
        System.out.println("Bem-vindo à aplicação, o que deseja fazer?\n\n1 - Login:\n2 - Registo:\n3 - Challenge:");

        Scanner sc = new Scanner(System.in);
        int opt = sc.nextInt();

        switch (opt) {
            case 1:
                Login();
                break;
            case 2:
                Registo();
                break;
            case 3:
                challenge();
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

        //for (int i = 0; i < 2048; i++) {
        //removeu-se o 2048 para funcionar com o chap

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
        pass  = String.format("%064x", new java.math.BigInteger(1, digest));

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
            //System.out.println(pub.toString());

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


    public static void Login(){

        System.out.println("Insira o username:");
        Scanner sc = new Scanner(System.in);
        String username = sc.nextLine();

        System.out.println("Insira a password:");
        String pw = sc.nextLine();

        //gerar um Id e um "nounce"

        byte[] nounce = Server.getNounce();
        int id = Server.getId();

        System.out.println("Nounce = "+nounce+"\nid = " +id);

        String salpassword = pw+Server.getSalt(username);
        System.err.println("Sal e password = " + salpassword);


        byte[] digest = null;
        String firstHash="";

        //calcular o hash da password com o sal do utilizador a querer autenticar

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            md.update(salpassword.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        digest = md.digest();
        firstHash = String.format("%064x", new java.math.BigInteger(1, digest));

        String toBeHashed = id+nounce.toString()+firstHash;
        System.out.println("(concatenação id | nounce | (hash pass | salt)) = " + toBeHashed);

        //calcular o valor de hash final para ser verificado no lado do servidor

        String lastHash="";

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
        lastHash = String.format("%064x", new java.math.BigInteger(1, digest));

        System.out.println("Hash enviado para o servidor = " + lastHash + "\n\n\n");

        //verificar se são iguais

        int verify = Server.VerifyAuth(id, nounce, username, pw, lastHash);
        if(verify == 1){
            nome = username;
            System.out.println("Utilizador autenticado com sucesso!\nBem-vindo(a) " + username);
        }
        else{
            System.err.println("Autenticação sem sucesso!\n");
            intro();
        }

    }

    //TODO corrigir o erro de sintaxe do sql, ainda nao consegui


}
