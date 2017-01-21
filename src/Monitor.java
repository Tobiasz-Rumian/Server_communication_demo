import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Monitor implements Runnable {

    private MonitorGui gui = new MonitorGui();
    private MonitorClock monitorClock;
    private static final int SERVER_PORT = 15000;
    private String host;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean kill = false;

    Monitor() {
        (new Thread(this)).start();
    }


    public void run() {
        try {
            host = InetAddress.getLocalHost().getHostName();
            socket = new Socket(host, SERVER_PORT);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject("$MONITOR$");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Polaczenie sieciowe dla klienta nie moze byc utworzone");
            kill();
            return;
        }
        monitorClock = new MonitorClock(this);
        try {
            while (!kill) {
                String s = (String) input.readObject();
                String nick="error",value="error";
                if (s.equals("$START$")) {
                    s = (String) input.readObject();
                    while(!s.equals("$END$")) {
                        value = s.substring(s.indexOf("$") + 1, s.length());
                        nick = s.substring(0, s.indexOf("$"));
                        gui.addGateway(nick,value);
                        s = (String) input.readObject();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Polaczenie sieciowe dla klienta zostalo przerwane");
            kill();
        }
    }

    void ping() {
        try {
            output.writeObject("$PING$");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void kill() {
        if (monitorClock != null) monitorClock.kill();
        gui.kill();
    }

    public static void main(String[] args) {
        new Monitor();
    }
}
