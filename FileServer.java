import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by chaomaer on 7/6/17.
 */
public class FileServer {
    public static void main(String[] args) {
        Hashtable<Integer,NodeInfo> nodetable = new Hashtable<>(); // FileServer最重要的一个变量,用来记录StrageNode中的信息
        // 使这个容器的操作变为atom操作
        Hashtable<String,ItemFile> filetable = new Hashtable<>();  //文件表,所有的文件信息都在这个table中进行存储
        // hashtable 本身就是线程安全的
        //注册线程
        RegisterThread registerThread = new RegisterThread(nodetable);
        registerThread.start();
        //定时线程
        ReceiveTimerThread timerThread = new ReceiveTimerThread(nodetable);
        timerThread.start();
        //文件操作线程
        FileopeThread  fileopeThread = new FileopeThread(nodetable,filetable);
        fileopeThread.start();
    }
}
