/**
 * Created by Tobiasz Rumian on 21.01.2017.
 */
public class MonitorClock implements Runnable{
    private boolean kill=false;
    private Monitor monitor;
    public MonitorClock(Monitor monitor){
        this.monitor=monitor;
        (new Thread(this)).start();
    }

    @Override
    public void run() {
        while(!kill){
            monitor.ping();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void kill(){
        kill=true;
    }
}
