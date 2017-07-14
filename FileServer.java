import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by chaomaer on 7/6/17.
 */
public class FileServer {
    public static void main(String[] args) {
        // 程序加载的时候，首先要加载初始化信息
        Hashtable<Integer,NodeInfo> nodetable = new Hashtable<>(); // FileServer最重要的一个变量,用来记录StrageNode中的信息
        // 使这个容器的操作变为atom操作
        Hashtable<String,ItemFile> filetable = new Hashtable<>();  //文件表,所有的文件信息都在这个table中进行存储
        // hashtable 本身就是线程安全的
        File file = new File("init.txt");
        if (file.exists()){
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                String ss;
                while (!(ss = ois.readUTF()).equals("over")){
                    try {
                        System.out.println(ss);
                        ItemFile itemFile = (ItemFile) ois.readObject();
                        filetable.put(ss, itemFile);
                        Utils.sendtoFileMonitor(itemFile);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                Integer port;
                while ((port = ois.readInt())!=0){
                    try {
                        NodeInfo nodeInfo = (NodeInfo) ois.readObject();
                        nodetable.put(port, nodeInfo);
                        System.out.println(port+"---"+nodeInfo);
                        Utils.sendtoNodeMonitor(nodeInfo);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 初始化完成
        //注册线程
        RegisterThread registerThread = new RegisterThread(nodetable);
        registerThread.start();
        //定时线程
        ReceiveTimerThread timerThread = new ReceiveTimerThread(nodetable);
        timerThread.start();
        //文件操作线程
        FileopeThread  fileopeThread = new FileopeThread(nodetable,filetable);
        fileopeThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("文件服务器正在退出....");
                // 当程序退出的时候首先保存
                File file = new File("init.txt");
                // 先保存file信息
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                    for (String s : filetable.keySet()) {
                        oos.writeUTF(s);
                        oos.writeObject(filetable.get(s));
                    }
                    oos.writeUTF("over");
                    for (Integer integer : nodetable.keySet()) {
                        oos.writeInt(integer);
                        nodetable.get(integer).canUse = false;
                        oos.writeObject(nodetable.get(integer));
                    }
                    oos.writeInt(0);
                    oos.writeObject("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
