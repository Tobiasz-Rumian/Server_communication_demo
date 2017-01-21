
public class Test {
    public static void main(String[] args) {
        new Server();
        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {
        }
        new Monitor();
        new User("Ewa");
        new User("Adam");
    }
}
