import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaomaer on 7/6/17.
 */
public class RegisterThread extends Thread {
    public static final int NODEINFOSIZE = 233;
    private byte[] buffer = new byte[NODEINFOSIZE];
    public List<NodeInfo> arrayList;// 用来存储注册的各个StotageNode
    public RegisterThread(List<NodeInfo> list){
        this.arrayList = list;
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
                        arrayList.add(nodeInfo);
                        System.out.println(nodeInfo.NodePort + "已经注册到了FileServer");
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
