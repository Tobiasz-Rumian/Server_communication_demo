import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class User implements Runnable {
    private UserClock userClock = new UserClock(this);
    private UserGui gui;
    private static final int SERVER_PORT = 15000;
    private String nick;
    private String host;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean kill = false;
    private int currentFrequency = 1000;
    private boolean gatewayBlocked = false;

    User(String nick) {
        this.nick = nick;
        (new Thread(this)).start();
    }


    public void run() {
        try {
            host = InetAddress.getLocalHost().getHostName();
            socket = new Socket(host, SERVER_PORT);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(nick);
            gui = new UserGui(nick, Integer.toString(socket.getPort()), host, this);//Tworzenie gui
        } catch (IOException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Polaczenie sieciowe dla klienta nie moze byc utworzone");
            kill();
            return;
        }
        try {
            while (!kill) {
                String s = (String) input.readObject();
                switch (s) {
                    case "$BLOCKED$":
                        gui.append("Brama ma status: " + s);
                        gatewayBlocked = true;
                        gui.gatewayStatus(gatewayBlocked);
                        userClock.freeze();
                        break;
                    case "$OPEN$":
                        gui.append("Brama ma status: " + s);
                        gatewayBlocked = false;
                        gui.gatewayStatus(gatewayBlocked);
                        break;
                    case "$TAKEN$":
                        System.err.println("Nick zajęty! Konczenie programu dla nicku: " + nick);
                        JOptionPane.showMessageDialog(
                                null,
                                "Nick zajęty! Kończenie programu.",
                                "Nick zajęty!",
                                JOptionPane.ERROR_MESSAGE);
                        kill();
                        return;
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Polaczenie sieciowe dla klienta zostalo przerwane");
            kill();
        }
    }

    void go() {
        try {
            output.writeObject("$GO$");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void goAuto() {
        userClock.changeFreezeState();
    }

    void setCurrentFrequency(int i) {
        currentFrequency = i;
    }

    int getCurrentFrequency() {
        return currentFrequency;
    }

    private void kill() {
        if (userClock != null) userClock.kill();
        gui.kill();
    }

    public static void main(String[] args) {
        String nick;

        nick = JOptionPane.showInputDialog("Podaj nazwe klienta");
        if (nick != null && !nick.equals("")) {
            new User(nick);
        }
    }
}

