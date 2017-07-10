

/**
 * Created by chaomaer on 7/6/17.
 */
public class FileServer {
    public static void main(String[] args) {
        //注册线程
        RegisterThread registerThread = new RegisterThread();
        registerThread.start();
        //定时线程
        ReceiveTimerThread timerThread = new ReceiveTimerThread(registerThread);
        timerThread.start();
        //文件操作线程
        FileopeThread  fileopeThread = new FileopeThread(registerThread);
        fileopeThread.start();
    }
}
