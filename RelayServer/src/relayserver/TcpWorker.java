package relayserver;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;

/**
 * @author 佳欢
 */
public class TcpWorker extends Thread {
    Socket client, source;
    InputStream in;
    OutputStream out;
    public TcpWorker( Socket Client, Socket Source, boolean NoSubWorker ) throws IOException {
        client = Client; source = Source;
        out = Client.getOutputStream(); in=Source.getInputStream(); // Source Response to Server.
        if (!NoSubWorker) new TcpWorker( Source, Client, true ).start();
    }
    private void releaseSocket() {
        try { client.close(); } catch(IOException e){}
        try { source.close(); } catch(IOException e){}
    }
    public void run() {
        try {
            while( true ) {
                out.write( in.read() );
            }
        } catch(IOException e) {
            releaseSocket();
        }
    }
    public static void trace(String Msg) { System.err.println(Msg); }
}
