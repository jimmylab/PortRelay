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
        //TcpClient client = new TcpClient("localhost",80,"localhost",780);
        TcpClient client = new TcpClient("office703.jimmylab.net",3389,"localhost",3489);
        client.begin();
    }
}


