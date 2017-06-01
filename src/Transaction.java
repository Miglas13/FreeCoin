import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Scanner;

/**
 * Created by Rui Santos on 27/05/2017.
 */
public class Transaction {


    Socket socket= null;

    public static void sign(File out, File pk, File sign){

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();
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
            FileOutputStream sigfos = new FileOutputStream(sign);
            sigfos.write(realSig);
            sigfos.close();

            // Save public key
            byte[] key = pub.getEncoded();
            FileOutputStream keyfos = new FileOutputStream(pk);
            keyfos.write(key);
            keyfos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    public static  void main (String argv[]) throws IOException {

        String destinatario = "";
        int montante=0;
        String PK, SK, PKD, SKD;

        Scanner scanner = new Scanner(System.in);


        System.out.println("Insira a sua chave pública");
        PK= (scanner.next());
        System.out.println("Insira o destinatário");
        destinatario=(scanner.next());

        System.out.println("Qual o montante que pretende transferir?");
        montante= (scanner.nextInt());

        System.out.println("Insira a chave pública do destinatário");
        PKD= (scanner.next());

        System.out.println("Insira o nome do ficheiro");

        //passar os dados para o ficheiro


        try {
            BufferedWriter out = new BufferedWriter(new FileWriter((scanner.next())+".txt"));
            out.write(PK + "\n");
            out.write(montante + "\n");
            out.write(PKD);
            //Assinar o ficheiro e enviar para o servidor
            sign(out, PKI,Sign);




        }
        catch (IOException e){
            System.out.println("Exception ");

        }




    }
}
