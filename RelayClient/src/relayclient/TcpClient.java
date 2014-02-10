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
        trace("Initializing Source="+LocalAddr+":"+LocalPort+", RelayerInput="+ServerAddr+":"+ServerInputPort);
        local = new InetSocketAddress(LocalAddr, LocalPort);
        remote = new InetSocketAddress(ServerAddr, ServerInputPort);
    }
    public void begin() {
        try {
            trace("Creating Tcp child...");
            new TcpChild(local, remote, 1).start();
        } catch(IOException e) {
            trace("Child creation failed.");
        }
    }
    public static void trace(String Msg) { System.err.println(Msg); }
}

class TcpChild extends Thread {
    //final int MaxChild = 50;
    //ChildNumer
    static final int SYN = 0x15; static final int ENQ = 0x5;
    int statues = 0;
    Socket remote; Socket local;
    SocketAddress localAddr;
    boolean srcClosed = true; boolean destClosed = true;
    int sequence = 0;
    public TcpChild(SocketAddress Local, SocketAddress Remote, int Sequence) throws IOException {
        local = new Socket(); remote = new Socket();
        trace("Connecting remote relayer "+Remote+", seq="+Sequence+"...");
        remote.connect(Remote);
        trace("Established connection with remote relayer "+remote.getInetAddress()+":"+remote.getPort()+"...");
        destClosed = false;
        localAddr = Local;
        sequence = Sequence;
    }
    @Override public void run() {
        try {
            InputStream in = remote.getInputStream();
            trace("Waiting for server SYN...");
            if (in.read() != SYN)
                throw new IOException("No SYN signal found!");
            statues = SYN;
            trace("SYN received, begin data transfering...");
            local.connect(localAddr);
            trace("Creating new child, Seq="+(sequence+1));
            new TcpChild(localAddr, remote.getRemoteSocketAddress(), sequence+1).start();
            
            trace("Local connection "+local.getInetAddress()+":"+local.getPort()+" established, seq="+sequence);
            TcpChildIO RemoteToLocal = new TcpChildIO(this, in, local.getOutputStream(), "Remote to local ");
            RemoteToLocal.start();
            TcpChildIO LocalToRemote = new TcpChildIO(this, local.getInputStream(), remote.getOutputStream(), "Local to remote ");
            LocalToRemote.start();
            
        } catch (IOException e) {
            trace("Error: "+e.getMessage());
            //e.printStackTrace();
            SocketAddress remoteAddr = remote.getRemoteSocketAddress();
            closeRes();
            if (statues!=SYN) {
                trace("Trying to reconnect, because previous connection was not established.");
                try { new TcpChild(localAddr, remoteAddr, sequence+1).start(); } catch(IOException f){}
            }
        }
    }
    public void closeRes() {
        closeRes("");
    }
    public void closeRes(String side) {
        try { remote.close(); }catch(Throwable e){}
        try { local.close(); }catch(Throwable e){}
        trace(side+"Connection between "+remote.getInetAddress()+":"+remote.getPort()+" and "+
                local.getInetAddress()+":"+local.getPort()+" closed, seq="+sequence);
    }
    public static void trace(String Msg) { System.err.println(Msg); }
}

class TcpChildIO extends Thread {
    TcpChild parent; InputStream I; OutputStream O;
    String side;
    public TcpChildIO(TcpChild Parent, InputStream I, OutputStream O, String Side ) {
        this.I = I; this.O = O;
        parent = Parent;
        side = Side;
    }
    @Override public void run() {
        try {
            while(true)
                O.write( I.read() );
        } catch(IOException e) {
            parent.closeRes(side);
        }
    }
    public static void trace(String Msg) { System.err.println(Msg); }
}