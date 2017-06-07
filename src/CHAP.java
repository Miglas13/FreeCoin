/**
 * Created by Rui Santos on 18/05/2017.
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CHAP
{

    //byte[] b = string.getBytes();


    public static byte[] chapSHA256(byte id, byte[] Password, byte[] Challenge) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(id);
        md.update(Password, 0, Password.length);
        md.update(Challenge, 0, Challenge.length);
        return md.digest();
    }

    public static byte[] chapResponse(byte id, byte[] Password, byte[] Challenge) throws NoSuchAlgorithmException
    {
        byte[] Response = new byte[17];
        Response[0] = id;
        System.arraycopy(chapSHA256(id, Password, Challenge), 0, Response, 1, 16);
        return Response;
    }
}