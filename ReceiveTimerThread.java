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
    public Hashtable<Integer,NodeInfo> nodetable;// 用来存储注册的各个StotageNode
    public ReceiveTimerThread(Hashtable<Integer,NodeInfo> nodetable){
        this.nodetable = nodetable;
    }
    private byte [] buffer = new byte[32];
    @Override
    public void run() {
        try {
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

    private void parsepacket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                DataInputStream dis = new DataInputStream(bis);
                try {
                    int portval = dis.readInt();
                    int command = dis.readInt();
                    if (command == -1){
                        System.out.println("服务器"+portval+"停止运行");
                        nodetable.get(portval).canUse = false;
                        Utils.sendtoNodeMonitor(nodetable.get(portval));
                    }else if (command == 0){
                        for (Integer integer : nodetable.keySet()) {
                            NodeInfo nodeInfo = nodetable.get(integer);
                            if (nodeInfo.nodePort == portval){
                                nodeInfo.starttime = System.currentTimeMillis();
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
