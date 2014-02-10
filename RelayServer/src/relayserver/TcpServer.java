package relayserver;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

public class TcpServer extends Thread {
    static final int SYN = 0x15;
    static final int DefaultParallels = 100;
    int inputPort; int portOut;
    ServerSocket fromClient;
    Socket fromSource;
    int ClientNum = 0;
    protected ArrayBlockingQueue<Socket> parallels;
    TcpReceiver receiver;
    public TcpServer(int InputPort, int OutputPort) throws IOException {
        this(InputPort, OutputPort, DefaultParallels);
    }
    public TcpServer(int InputPort, int OutputPort, int Parallels) throws IOException {
        inputPort = InputPort; portOut = OutputPort;
        fromClient = new ServerSocket(OutputPort);
        trace("Server Listening localhost:"+OutputPort+" ...");
        parallels = new ArrayBlockingQueue(Parallels);
        trace("Initialized request queue, starting receiving listener "+InputPort);
        receiver = new TcpReceiver(parallels, inputPort);
    }
    @Override public void run() {
        Socket client=null;
        int seq=0;
        receiver.start();
        while( ++seq > 0) {
            try {
                trace("Waiting for requests...");
                client = fromClient.accept();
                trace("Find a request from "+client.getInetAddress()+":"+client.getPort()+
                        ", client-side seq="+seq+" adding to the queue...");
                parallels.add(client);
                trace("Client-side request from "+client.getInetAddress()+":"+client.getPort()+
                        ", seq="+seq+" was added successfully.");
                receiver.interrupt();
            } catch (IllegalStateException e) {
                trace("The queue is full, dropping clients...");
                try { client.close(); } catch(Throwable ex){}
                while( parallels.remainingCapacity()>0 ) {
                    try { fromClient.accept().close(); } catch(IOException ex){}
                }
            } catch(IOException ex) {
                trace("Failed while establishing accepting clients.");
                try { Thread.sleep(1000); } catch(InterruptedException f){}
                break;
            }
        }
    }
    public static void trace(String Msg) { System.err.println(Msg); }
}
