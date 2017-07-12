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
    private static final long period = 1000*60*5;
    public Hashtable<Integer,NodeInfo> nodetable;// 用来存储注册的各个StotageNode
    public ReceiveTimerThread(Hashtable<Integer,NodeInfo> nodetable){
        this.nodetable = nodetable;
    }
    private byte [] buffer = new byte[32];
    @Override
    public void run() {
        try {
            starttimer();
            DatagramSocket socket = new DatagramSocket(9999);
            while (true){
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                socket.receive(packet);
                parsepacket();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void starttimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("正在检查是否有StorageNode掉线...");
                for (Integer integer : nodetable.keySet()) {
                    NodeInfo nodeInfo = nodetable.get(integer);
                    if (nodeInfo.canUse && (System.currentTimeMillis() - nodeInfo.starttime > period)) {
                        nodeInfo.canUse = false;
                    }
                }
                int sum = 0;
                for (Integer integer : nodetable.keySet()) {
                    NodeInfo nodeInfo = nodetable.get(integer);
                    if (nodeInfo.canUse ) {
                        sum++;
                    }
                }
                System.out.println("当前可用的服务器台数"+sum);
            }
        },0,period);
    }

    private void parsepacket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                DataInputStream dis = new DataInputStream(bis);
                try {
                    int portval = dis.readInt();
                    for (Integer integer : nodetable.keySet()) {
                        NodeInfo nodeInfo = nodetable.get(integer);
                        if (nodeInfo.nodePort == portval){
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
