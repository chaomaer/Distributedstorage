import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chaomaer on 7/6/17.
 */
public class FileServer {
    public static void main(String[] args) {
        List<NodeInfo> arrayList = new ArrayList<>(); // FileServer最重要的一个变量
        arrayList = Collections.synchronizedList(arrayList); // 使这个容器的操作变为atom操作
        //注册线程
        RegisterThread registerThread = new RegisterThread(arrayList);
        registerThread.start();
        //定时线程
        ReceiveTimerThread timerThread = new ReceiveTimerThread(arrayList);
        timerThread.start();
        //文件操作线程
        FileopeThread  fileopeThread = new FileopeThread(arrayList);
        fileopeThread.start();
    }
}
