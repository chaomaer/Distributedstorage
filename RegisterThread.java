import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by chaomaer on 7/6/17.
 */
public class RegisterThread extends Thread {
    public NodeInfo nodeInfo;
    public byte[] buffer = new byte[213];
    public ArrayList<Integer> arrayList = new ArrayList<>(); // 用来存储注册的各个StotageNode
    @Override
    public void run() {
            try {
                DatagramSocket ss = new DatagramSocket(2222); // 用来监听StotageNode的注册信息
                while (true){
                    DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                    ss.receive(packet);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                            try {
                                ObjectInputStream ois = new ObjectInputStream(bais);
                                try {
                                    NodeInfo nodeInfo = (NodeInfo) ois.readObject();
                                    arrayList.add(nodeInfo.NodePort);
                                    System.out.println(nodeInfo.NodePort+"已经注册到了FileServer");
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
