import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Gateway implements Runnable {

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private int gatewayCounter = 0;
    private boolean gatewayBlocked = false;
    private String nick;
    private Server server;
    private volatile boolean kill = false;

    Gateway(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        Thread t = new Thread(this);
        t.start();
    }

    String getNick() {
        return nick;
    }

    ObjectInputStream getInput() {
        return input;
    }

    public String toString() {
        return nick;
    }

    void changeStateOfGateway() {
        gatewayBlocked = !gatewayBlocked;
    }

    boolean isGatewayBlocked() {
        return gatewayBlocked;
    }

    int getGatewayCounter() {
        return gatewayCounter;
    }

    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            nick = (String) input.readObject();
            if (server.isNickTaken(nick)) {
                output.writeObject("$TAKEN$");
                input.close();
                output.close();
                socket.close();
                socket = null;
                return;
            } else if (nick.equals("$MONITOR$")) {
                server.addMonitor(this);
                while (!kill) server.useMonitor(this);
                input.close();
                output.close();
                socket.close();
                socket = null;
                return;
            }
            server.addClient(nick, this);
            while (!kill) server.useCommand(this);
            server.deleteClient(this);
            input.close();
            output.close();
            socket.close();
            socket = null;
        } catch (Exception ignored) {
        }
    }

    void addOne() {
        gatewayCounter++;
    }

    void out(String s) {
        try {
            output.writeObject(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
