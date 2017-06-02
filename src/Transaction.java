import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

/**
 * Created by Rui Santos on 27/05/2017.
 */
public class Transaction {


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


    public static FileOutputStream sign(FileInputStream out, String pk, File sign) throws FileNotFoundException {

        FileOutputStream sigfos = new FileOutputStream(sign);
        try {

            PrivateKey priv = stringToPrivateKey(pk);
            Signature dsa = Signature.getInstance("SHA1withECDSA");
            dsa.initSign(priv);
            BufferedInputStream bufin = new BufferedInputStream(out);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            }

            bufin.close();
            byte[] realSig = dsa.sign();

            // Save signature
            sigfos.write(realSig);
            sigfos.close();
            return  sigfos;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sigfos;


    }


    public static  void main (String argv[]) throws IOException {



        //passar os dados para o ficheiro


       // try {
            //BufferedWriter out = new BufferedWriter(new FileWriter((scanner.next())+".txt"));
            //out.write(PK + "\n");
            //out.write(montante + "\n");
            //out.write(PKD);
            //Assinar o ficheiro e enviar para o servidor
            //sign(out, PKI,Sign);




        //}
        //catch (IOException e){
          //  System.out.println("Exception ");

        //}




    }
}
