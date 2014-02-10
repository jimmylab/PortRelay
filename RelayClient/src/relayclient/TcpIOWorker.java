package relayclient;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author 佳欢
 */
public class TcpIOWorker extends Thread {
    InputStream in; OutputStream out;
    final Socket firstly; final Socket secondly;
    public TcpIOWorker( Socket Firstly, Socket Secondly ) throws IOException {
        firstly = Firstly; secondly = Secondly;
        out = firstly.getOutputStream(); in = secondly.getInputStream();
    }
    @Override public void run() {
        try {
            TcpIOWorker another = new TcpIOWorker(secondly, firstly);
            another.start();
            while(true)
                out.write( in.read() );
        } catch(IOException e) {
            closeRes();
        }
    }
    void closeRes() {
        try {
            synchronized(firstly) { firstly.close(); }
        } catch(IOException e) {}
        try {
            synchronized(secondly) { secondly.close(); }
        } catch(IOException e) {}
    }
}
