package relayclient;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TcpClient {
    SocketAddress local;
    SocketAddress remote;
    public TcpClient(String LocalAddr, int LocalPort, String ServerAddr, int ServerInputPort) {
        local = new InetSocketAddress(LocalAddr, LocalPort);
        remote = new InetSocketAddress(ServerAddr, ServerInputPort);
    }
    public void begin() {
        try {
            new TcpChild(local, remote).start();
        } catch(IOException e) {}
    }
}

class TcpChild extends Thread {
    //final int MaxChild = 50;
    //ChildNumer
    static final int SYN = 0x15; static final int ENQ = 0x5;
    static int statues = 0;
    Socket remote; Socket local;
    SocketAddress localAddr;
    boolean srcClosed = true; boolean destClosed = true;
    public TcpChild(SocketAddress Local, SocketAddress Remote) throws IOException {
        local = new Socket(); remote = new Socket();
        remote.connect(Remote);
        destClosed = false;
        localAddr = Local;
    }
    @Override public void run() {
        try {
            InputStream in = remote.getInputStream();
            if (in.read() != SYN)
                throw new IOException("No SYN signal found!");
            statues = SYN;
            if (in.read() != ENQ) {
                throw new IOException("No ENQ signal found!");
            }
            statues = ENQ;
            remote.connect(localAddr);
            new TcpChild(localAddr, remote.getRemoteSocketAddress()).start();
            
            TcpChildIO RemoteToLocal = new TcpChildIO(this, in, local.getOutputStream());
            RemoteToLocal.start();
            TcpChildIO LocalToRemote = new TcpChildIO(this, local.getInputStream(), remote.getOutputStream());
            LocalToRemote.start();
            
        } catch (IOException e) {
            SocketAddress remoteAddr = remote.getRemoteSocketAddress();
            if (statues==SYN)
                try { new TcpChild(localAddr, remoteAddr).start(); } catch(IOException f){}
            closeRes();
        }
    }
    public void closeRes() {
        try { remote.close(); }catch(Throwable e){}
        try { local.close(); }catch(Throwable e){}
    }
}

class TcpChildIO extends Thread {
    TcpChild parent; InputStream I; OutputStream O;
    public TcpChildIO(TcpChild Parent, InputStream I, OutputStream O ) {
        this.I = I; this.O = O;
        parent = Parent;
    }
    @Override public void run() {
        try {
            while(true)
                O.write( I.read() );
        } catch(IOException e) {
            parent.closeRes();
        }
    }
}