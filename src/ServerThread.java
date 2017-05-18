import com.sun.xml.internal.ws.api.message.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

public class ServerThread extends Thread{

    static final String dbName = "/home/andremigueldasilvapinho/frbased.db";

    public String serverRequest = "none";
    private Socket socket = null;
    private int counter = 0;
    byte[] signature;

    private final List<ServerThread> killList;
    private final List<ServerThread> serverActions;

    public UUID serverThreadID;
    private int maxLocalRetries;

    public ServerThread(Socket socket, List<ServerThread> killList, List<ServerThread> serverActions){
        super("ServerThread");
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

    public static void main(String[] args) throws IOException{

    }
}