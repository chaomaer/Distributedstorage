

/**
 * Created by chaomaer on 7/6/17.
 */
public class FileServer {
    public static void main(String[] args) {
        RegisterThread registerThread = new RegisterThread();
        registerThread.start();
        FileopeThread  fileopeThread = new FileopeThread(registerThread);
        fileopeThread.start();
    }
}
