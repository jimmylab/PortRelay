package relayserver;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.net.SocketException;

public class TcpReceiver extends Thread  {
    static final int SYN = 0x15;
    ServerSocket sourceServer;
    ArrayBlockingQueue<Socket> workLine;
    public TcpReceiver( ArrayBlockingQueue<Socket> Parallels, int InputPort ) throws IOException {
        sourceServer = new ServerSocket(InputPort);
        trace("Listening source from localhost:"+InputPort+" ...");
        workLine = Parallels;
    }
    public void run() {
        Socket client=null;
        Socket source=null;
        while(true) {
            try {
                trace("Wait for a client from work-line...");
                client = workLine.take();
                //client = workLine.poll(1, TimeUnit.SECONDS);
            } catch(InterruptedException e) {
                //
            }
            try {
                trace("Client fetched, waiting for a source server...");
                source = sourceServer.accept();
                trace("Server fetched, opening the pass between from server to client...");
                if ( client != null ) {
                    source.setTcpNoDelay(true);
                    source.getOutputStream().write(SYN);
                    source.setTcpNoDelay(false);
                    new TcpWorker(client, source, false).start();
                }
                trace("The pass opened succeessfully.");
            } catch(IOException f) {
                try { client.close(); } catch(Throwable g){}
                try { source.close(); } catch(Throwable g){}
                trace("Error while opening the pass between from server to client.");
            }
        }
    }
    public static void trace(String Msg) { System.err.println(Msg); }
}
