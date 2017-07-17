import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by chaomaer on 7/6/17.
 */
public class FileopeThread extends Thread {
    public Hashtable<String,ItemFile> filetable = new Hashtable<>();
    public Hashtable<Integer,NodeInfo> nodetable;

    public FileopeThread(Hashtable<Integer, NodeInfo> nodetable, Hashtable<String, ItemFile> filetable) {
        this.filetable = filetable;
        this.nodetable = nodetable;
    }


    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(3333); // 用来监听FileClient端的信息
            while (true){
                Socket socket = ss.accept();
                startfileop(socket);
//                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startfileop(Socket socket){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    int command = dataInputStream.readInt();
                    switch (command){
                        case 0:
                            // 代表上传文件，这需要进行对FileServer,StorageNode的值进行修改
                            parseupload(dataInputStream,dataOutputStream);
//                            socket.close();
                            break;
                        case 1:
                            // 文件下载，没有影响
                            parseupdownload(dataInputStream,dataOutputStream);
                            break;
                        case 2:
                            // 文件删除,这需要进行对FileServer,StorageNode的值进行修改
                            parsedelete(dataInputStream,dataOutputStream);
                    }
                } catch (IOException e) {
                    System.out.println();
                }
            }
        }).start();
    }

    private void parsedelete(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        try {
            String key = dataInputStream.readUTF();
            if (!filetable.containsKey(key)){
                dataOutputStream.writeInt(0);
                dataOutputStream.flush();
                return;
            }
            ItemFile itemFile = filetable.get(key);
            int val1 = itemFile.port1;
            int val2 = itemFile.port2;
            nodetable.get(val1).filenum--;
            nodetable.get(val2).filenum--;
            changeVolume(0,val1,itemFile.filelen);
            changeVolume(0,val2,itemFile.filelen);
            dataOutputStream.writeInt(1);
            ObjectOutputStream objectOutput = new ObjectOutputStream(dataOutputStream);
            objectOutput.writeObject(itemFile);
            dataOutputStream.flush();
            sendtoNodeMonitor(val1);
            sendtoNodeMonitor(val2);
            sendtoFileMonitor(key);
            // 同时删除在FileServer中的记录
            filetable.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseupdownload(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        boolean flag;
        try {
            System.out.println(dataOutputStream.size());
            String key = dataInputStream.readUTF();
            flag = filetable.containsKey(key);
            if (flag){
                dataOutputStream.writeInt(1);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(dataOutputStream);
                objectOutputStream.writeObject(filetable.get(key));
                objectOutputStream.flush();
            }else {
                dataOutputStream.writeInt(0); // 代表没有这个文件  //太迷了.....
                dataOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseupload(DataInputStream dataInputStream , DataOutputStream dataOutputStream){
        String filename = null;
        try {
            filename = dataInputStream.readUTF();
            String uuid = UUID.randomUUID().toString();
            long filelen = dataInputStream.readLong();
            System.out.println(filename+" "+filelen);
            String vals = SLB(nodetable);
            int val1 = Integer.parseInt(vals.substring(0,vals.indexOf("&")));
            int val2 = Integer.parseInt(vals.substring(vals.indexOf("&")+1,vals.length()));
            // 填表
            if (val1==0){
                dataOutputStream.writeUTF("empty");
            }else {
                filetable.put(uuid,new ItemFile(uuid,filename,filelen,val1,val2));
                // 文件数量+1;
                // 对应的存储容量
                changeVolume(1,val1,filelen);
                changeVolume(1,val2,filelen);
                nodetable.get(val1).filenum++;
                nodetable.get(val2).filenum++;
                dataOutputStream.writeUTF(uuid);
                dataOutputStream.writeInt(val1);
                dataOutputStream.writeInt(val2);
                dataOutputStream.flush();
                //分别发送消息到两个监听器
                sendtoNodeMonitor(val1);
                sendtoNodeMonitor(val2);
                sendtoFileMonitor(uuid);
            }
            System.out.println("发送消息到FileClient端\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendtoFileMonitor(String uuid) {
        try {
            DatagramSocket socket = new DatagramSocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(filetable.get(uuid));
            byte[] buffer = bos.toByteArray();
            // 端口6666负责接受NodeMonitor的显示工作
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,InetAddress.getLocalHost(),6666);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendtoNodeMonitor(int val) {
        try {
            DatagramSocket socket = new DatagramSocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(nodetable.get(val));
            byte[] buffer = bos.toByteArray();
            // 端口6666负责接受NodeMonitor的显示工作
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,InetAddress.getLocalHost(),6667);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeVolume(int type,int val1,long len) {
        String s = nodetable.get(val1).remainVolume;
        String ss;
        float val ;
        if (s.substring(s.length()-2,s.length()) .equals("GB")){
            val = Float.parseFloat(s.substring(0,s.length()-2))*1024*1024*1024;
        }else {
            val = Float.parseFloat(s.substring(0,s.length()-2))*1024*1024;
        }
        switch (type){
            case 0:
                val += len;
                break;
            case 1:
                val -= len;
                break;
        }
        if (val/(1024*1024*1024)>1){
            ss = Float.toString(val/(1024*1024*1024))+"GB";
        }else {
            ss = Float.toString(val/(1024*1024))+"MB";
        }
        nodetable.get(val1).remainVolume = ss;
    }

    private String SLB(Hashtable<Integer,NodeInfo> table) {
        ArrayList<NodeInfo> arrayList = new ArrayList<>();
        for (Integer integer : table.keySet()) {
            NodeInfo nodeInfo = table.get(integer);
            if (nodeInfo.canUse) arrayList.add(nodeInfo);
        }
        System.out.println("目前的服务器台数量:");
        System.out.println(arrayList.size());
        if (arrayList.size()<=1){
            return "0&0";
        }
        Random random = new Random();
        int val1 = random.nextInt(arrayList.size());
        int val2 = random.nextInt(arrayList.size());
        while (val2==val1){
            val2 = random.nextInt(arrayList.size());
        }
        val1 = arrayList.get(val1).nodePort;
        val2 = arrayList.get(val2).nodePort;
        System.out.println(val1+"&"+val2);
        return val1+"&"+val2;
    }
    private void printFiletable(){
        for (ItemFile itemFile : filetable.values()) {
            System.out.println(itemFile);
        }
    }
    private void printNodetable(){
        for (NodeInfo nodeInfo : nodetable.values()) {
            System.out.println(nodeInfo);
        }
    }
}