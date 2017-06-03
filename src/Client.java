import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
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
    public static String nome = null;

    public static void solveChallenge(int binary, Socket socket){
        try {
            System.out.println("Ficheiro a enviar (PATH):");
            Scanner scanner = new Scanner(System.in);
            String file_to_send = scanner.nextLine();
            File file = new File(file_to_send);
            if (file.exists() && !file.isDirectory()){
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("1");
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
                    Server.updatebd(nome);
                }else {
                    System.out.println("Falhaste o desafio. Desculpa...");
                }
            }else{
                System.out.println("Ficheiro não existe.\n" +
                        "Challenge terminado. Sorry...\n");
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("Wrong File");
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
                System.out.println(length);
                byte[] message = new byte[length];
                dataInputStream.read(message);
                String s = new String(message,"US-ASCII");
                System.out.println(s);
                System.out.println("Deseja resolver o challenge [y/n]?");
                Scanner scanner = new Scanner(System.in);
                String a = scanner.nextLine();
                if(a.equals("y")){
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF("y");
                    solveChallenge(Integer.parseInt(s),socket);
                }else{
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF("n");
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        intro();
        intro2();
        challenge();
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
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("y");
                challenge(socket);
                intro2();
            }
            else{
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("n");
                intro2();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static PrivateKey stringToPrivateKey(String s) {

        BASE64Decoder decoder = new BASE64Decoder();
        byte[] c = null;
        KeyFactory keyFact = null;
        PrivateKey returnKey = null;

        try {

            c = decoder.decodeBuffer(s);
            keyFact = KeyFactory.getInstance("DSA", "SUN");
        } catch (Exception e) {

            System.out.println("Error in first try catch of stringToPrivateKey");
            e.printStackTrace();
        }


        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(c);
        try {   //the next line causes the crash
            returnKey = keyFact.generatePrivate(x509KeySpec);
        } catch (Exception e) {

            System.out.println("Error in stringToPrivateKey");
            e.printStackTrace();
        }

        return returnKey;

    }


    public static void sign(File out, String pk, String siga) throws FileNotFoundException {
        try {
            File sig = new File(siga);
            FileOutputStream sign = new FileOutputStream(sig);
            PrivateKey priv = stringToPrivateKey(pk);
            Signature dsa = Signature.getInstance("SHA1withECDSA");
            dsa.initSign(priv);
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


    public static void signAvancada(File out, String userEm, String siga) throws FileNotFoundException {
        try {
            File sig = new File(siga);
            FileOutputStream sign = new FileOutputStream(sig);
            PrivateKey privEm = null;
            String pubkeyEm = "";

            KeyPairGenerator keyGen = null;

            keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            KeyPair pair = keyGen.generateKeyPair();
            privEm = pair.getPrivate();
            String filename= userEm + ".sk";
            File f = new File(filename);
            f.delete();

            String filename2 = userEm+".sk";
            try (PrintStream out2 = new PrintStream(new FileOutputStream(filename))) {
                out2.print(privEm.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            PublicKey pub = pair.getPublic();
            pubkeyEm = pub.toString();

            Server.updatePubKey(userEm,pubkeyEm);
                //System.out.println(pub.toString());
            Signature dsa = Signature.getInstance("SHA1withECDSA");
            dsa.initSign(privEm);
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




    public static void Transation(String user){



            String destinatario = "";
            int montante=0;
            String PK, SK, PKD, SKD, username;

            Scanner scanner = new Scanner(System.in);
            PK = Server.getPubKey(user);
            System.out.println("Chave publica do user: "+ PK);
            System.out.println("Insira o username destinatário");
            destinatario=(scanner.next());
            PKD= Server.getPubKey(destinatario);
            System.out.println("Qual o montante que pretende transferir?");
            montante= (scanner.nextInt());

            String sk= "C:\\Users\\Rui Santos\\IdeaProjects\\FreeCoin\\FreeCoin\\"+ nome + ".sk";

            String secretKey = "";
            String linha= "\n";

            try {
                File file = new File("fich.txt");
                FileOutputStream f = new FileOutputStream(file);
                Writer w = new OutputStreamWriter(f, "UTF-8");
                w.write(PK + "\n");
                w.write(PKD + "\n");
                w.write(montante+"\n");
                System.out.println("Aqqqqsdfsdgvzv");
                BufferedReader br = null;
                FileReader fr = null;


                    fr = new FileReader(sk);
                    br = new BufferedReader(fr);

                    String sCurrentLine;

                    br = new BufferedReader(new FileReader(sk));

                    while ((sCurrentLine = br.readLine()) != null) {
                        System.out.println(sCurrentLine);
                        secretKey=sCurrentLine;
                    }

                String filename=user + destinatario+".sign";
                System.out.println("Aqqqq3");
                //cifrar file

                String key= "Não sejas Inácio";
                File encrypt= new File(filename+".encrypt");
                CryptoUtils.encrypt(key, "fich.txt", encrypt);

                //Assinar o ficheiro e enviar para o servidor

                signAvancada(encrypt,secretKey,filename);
                System.out.println("Aqqqq4");
                Server.verifyTransaction(user+destinatario+".sign", PK, PKD, montante);
                System.out.println("Aqqqq5");


            }
            catch (IOException e){
              System.out.println("Exception ");

            }







    }

    public static void intro(){
        System.out.println("Bem-vindo à aplicação, o que deseja fazer?\n\n1 - Login:\n2 - Registo:\n");

        Scanner sc = new Scanner(System.in);
        int opt = sc.nextInt();

        switch (opt) {
            case 1:
                Login();
                break;
            case 2:
                Registo();
                break;
        }

    }

    public static void intro2(){
        System.out.println("FR€coin\n\n1 - REALIZAR TRANSAÇAO:\n2 - VERIFICAR AS SUAS TRANSAÇÕES:\n");

        Scanner sc = new Scanner(System.in);
        int opt = sc.nextInt();

        switch (opt) {
            case 1:
                Transation(nome);
                break;
            case 2:
                Server.getMyTransactions(nome);
                break;
        }

    }

    public static void Registo(){

        Scanner sc = new Scanner(System.in);

        int noDups=0;
        do {
            System.out.println("Insira um nome para Login:\n");

            nome = sc.nextLine();

            noDups = Server.VerifyDups(nome);
            if (noDups != 0)
                System.out.println("Esse username já existe!\nTente outra vez");

        }
        while(noDups!=0);


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
        //Aqui a extensão é sk porque em inglês é secret key, se não, confunde-se

        String filename = nome+".sk";
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
            intro2();
            challenge();
        }
        else{
            System.err.println("Autenticação sem sucesso!\n");
            intro();
        }

    }


}
