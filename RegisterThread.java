import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by chaomaer on 7/6/17.
 */
public class RegisterThread extends Thread {
    public static final int NODEINFOSIZE = 1024;  // 此处有一些偷懒，理解为协议吧
    private byte[] buffer = new byte[NODEINFOSIZE];
    public Hashtable<Integer,NodeInfo> nodetable;// 用来存储注册的各个StotageNode
    public RegisterThread(Hashtable<Integer,NodeInfo> nodetable){
        this.nodetable = nodetable;
    }
    @Override
    public void run() {
        try {
            DatagramSocket ss = new DatagramSocket(2222); // 用来监听StotageNode的注册信息
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                ss.receive(packet);
                register();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void register() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                try {
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try {
                        NodeInfo nodeInfo = (NodeInfo) ois.readObject();
                        int port = nodeInfo.nodePort;
                        nodetable.put(port,nodeInfo);
                        System.out.println(nodeInfo.nodePort + "注册到了FileServer");
                        System.out.println(nodeInfo.toString());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
