package relayserver;
import java.io.IOException;

public class RelayServer {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
         * Local port 80
         * Remote in 780
         * Remote out 1780
         */
        //try { new TcpServer(780, 1780).start(); } catch(IOException e){}
        try { new TcpServer(3489, 33890).start(); } catch(IOException e){}
    }
}
