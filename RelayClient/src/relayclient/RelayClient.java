package relayclient;

public class RelayClient {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
         * Local port 80
         * Remote in 780
         * Remote out 1780
         */
        TcpClient client = new TcpClient("localhost",80,"localhost",780);
        client.begin();
    }
}


