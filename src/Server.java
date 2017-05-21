import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by andremigueldasilvapinho on 16-05-2017.
 */
public class Server {
    public final static int SOCKET_PORT = 13267;
    public final static String SERVER = "127.0.0.1";
    static String bitNumber;
    public static void main(String[] args) throws IOException {
        List<ServerThread> connectedClients = new Vector<>();
        List<ServerThread> killList         = new Vector<>();
        List<ServerThread> serverActions    = new Vector<>();
        int                portNumber       = 1367;
        boolean            listening        = true;
        createDB(ServerThread.dbName);
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
                while (listening) {
                    System.out.print("listening..");
                    ServerThread st = new ServerThread(serverSocket.accept(), killList, serverActions);
                    System.out.println("New client " + st.toString());
                    if (connectedClients.add(st)) {
                        st.start();
                    } else {
                        System.out.println("Could not connect client");
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not listen on port " + portNumber);
                System.exit(-1);
            }
        }).start();
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
}
