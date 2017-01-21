public class UserClock implements Runnable {
    private boolean kill = false;
    private boolean freeze = true;
    private User user;

    UserClock(User user) {
        this.user = user;
        (new Thread(this)).start();
    }

    @Override
    public void run() {
        while (!kill) {
            while (freeze) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            user.go();
            try {
                Thread.sleep(user.getCurrentFrequency());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void kill() {
        kill = true;
    }

    void freeze() {
        freeze = true;
    }

    void changeFreezeState() {
        freeze = !freeze;
    }
}
