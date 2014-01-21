package relayserver;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer extends Thread {
    static final int SYN = 0x15;
    int portIn; int portOut;
    ServerSocket source; ServerSocket client;
    Socket fromSource;
    int clients = 0;
    public TcpServer(int InputPort, int OutputPort) throws IOException {
        portIn = InputPort; portOut = OutputPort;
        source = new ServerSocket(InputPort);
    }
    @Override public void run() {
        while(true) {
            try {
                fromSource = source.accept();
                fromSource.setTcpNoDelay(true);
                fromSource.getOutputStream().write(SYN);
                clients--;
            } catch(IOException e) {
                try { Thread.sleep(1000); } catch(InterruptedException f){}
                break;
            }
            while ( clients>0 )
                try { Thread.sleep(6000); }
                catch(InterruptedException f){ break; }
            }
        }
    }
}
