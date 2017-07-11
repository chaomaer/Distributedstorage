import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

/**
 * Created by chaomaer on 7/6/17.
 */
public class RegisterThread extends Thread {
    private byte[] buffer = new byte[233];
    public ArrayList<NodeInfo> arrayList = new ArrayList<>(); // 用来存储注册的各个StotageNode

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
