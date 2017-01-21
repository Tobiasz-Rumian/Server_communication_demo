import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Runnable {
    private ServerGui gui = new ServerGui(this);
    private HashMap<String, Gateway> gateways = new HashMap<>();
    private ArrayList<Gateway> monitors = new ArrayList<>();
    private volatile boolean kill = false;
    private static final int SERVER_PORT = 15000;
    private String host;
    private ServerSocket server;


    Server() {
        (new Thread(this)).start();
    }

    synchronized void addClient(String nick, Gateway gateway) {
        gateways.put(nick, gateway);
        gui.addGateway(nick);
    }

    synchronized void deleteClient(Gateway gateway) {
        gateways.remove(gateway.getNick());
    }


    public void run() {
        Socket s;
        try {
            host = InetAddress.getLocalHost().getHostName();
            server = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Gniazdko dla Servera nie może być utworzone");
            gui.kill();
            System.exit(0);
        }
        System.out.println("Server zostal uruchomiony na hoscie " + host);

        while (!kill) {
            try {
                s = server.accept();
                if (s != null) new Gateway(this, s);
            } catch (IOException e) {
                System.out.println("BLAD Servera: Nie mozna polaczyc sie z klientem ");
            }
        }
        try {
            server.close();
            gui.kill();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new Server();
    }

    void useCommand(Gateway gateway) {
        String s = "error";
        try {
            s = (String) gateway.getInput().readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (gateway.isGatewayBlocked()) gateway.out("$BLOCKED$");
        else if (s.equals("$GO$")) {
            gateway.addOne();
        } else {
            System.err.println("Błąd podczas odbierania komendy");
        }
    }

    void useMonitor(Gateway monitor) {
        String s = "error";
        try {
            s = (String) monitor.getInput().readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (s.equals("$PING$")) {
            monitor.out("$START$");
            gateways.forEach((k, v) -> monitor.out(k + "$" + Integer.toString(v.getGatewayCounter())));
            monitor.out("$END$");
        } else {
            System.err.println("Błąd podczas odbierania komendy");
        }
    }

    void changeStateOfGateway(String s) {
        gateways.get(s).changeStateOfGateway();
        if (gateways.get(s).isGatewayBlocked()) gateways.get(s).out("$BLOCKED$");
        else gateways.get(s).out("$OPEN$");
    }

    boolean isNickTaken(String nick) {
        return gateways.containsKey(nick);
    }

    void addMonitor(Gateway monitor) {
        monitors.add(monitor);
    }
}



