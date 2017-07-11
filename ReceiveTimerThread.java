import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

/**
 * Created by chaomaer on 7/9/17.
 */
public class ReceiveTimerThread extends Thread {
    private Timer timer = new Timer();
    private ArrayList<NodeInfo> arrayList;
    private static final long period = 1000*2*3;
    public ReceiveTimerThread(RegisterThread registerThread){
        arrayList = registerThread.arrayList;
    }
    private byte [] buffer = new byte[32];
    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(9999);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("正在检查是否有StorageNode掉线...");
                    arrayList.removeIf(nodeInfo -> System.currentTimeMillis() - nodeInfo.starttime > period);
                    System.out.println("目前服务的台数"+arrayList.size());
                }
            },0,period);
            while (true){
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                socket.receive(packet);
                parsepacket();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parsepacket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                DataInputStream dis = new DataInputStream(bis);
                try {
                    int portval = dis.readInt();
                    for (NodeInfo nodeInfo : arrayList) {
                        if (nodeInfo.NodePort == portval){
                            nodeInfo.starttime = System.currentTimeMillis();
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
