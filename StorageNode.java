import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chaomaer on 7/6/17.
 */
public class StorageNode {
    public static NodeInfo nodeInfo;
    public static int listeningport;
    public static Timer timer;
    public static String rootFolder;
    //定时发送数据包的时间为2分钟
    public static final long period = 1000*60*2;
    // 当程序退出的时候,需要向FileServer发送离开的信息包
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                sendpacket(-1,listeningport);
                System.out.println("发送了一个断开连接连接数据包");
            }
        }));
        readinitfile();
        listeningport = nodeInfo.nodePort;
        rootFolder = nodeInfo.rootFolder;
        initrootFolder();
        System.out.println("初始化的StorageNode信息为"+nodeInfo);
        System.out.println("storageNode is ready in port "+listeningport);
        registertoFileServer(listeningport);
        // 2分钟向FileServer发送一次数据包
        startTimer();
        try {
            ServerSocket serverSocket = new ServerSocket(listeningport);
            while (true){
                Socket socket = serverSocket.accept();
                String ss;
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                int command = dataInputStream.readInt();
                switch (command){
                    case 0://代表存储文件，同时发送备份文件
                        Utils.StorageAndbackup(rootFolder,dataInputStream);
                        break;
                    case 1://代表为备份文件，需要在本主机进行备份
                        Utils.FileBackup(rootFolder,dataInputStream);
                        break;
                    case 2:
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        Utils.FileDownload(rootFolder,dataInputStream,dataOutputStream);
                        break;
                    case 3:
                        String filename = dataInputStream.readUTF();
                        System.out.println(filename);
                        File file = new File(rootFolder,filename);
                        file.delete();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initrootFolder() {
        File file = new File(rootFolder);
        if (!file.exists()) {
            file.mkdirs();
            nodeInfo.setFilenum(0);
            nodeInfo.setRemainVolume(nodeInfo.volume);
        }else {
            nodeInfo.setFilenum(file.list().length);
            long sum = 0;
            String[] files = file.list();
            for (String s : files) {
                file = new File(rootFolder,s);
                sum += file.length();
            }
            System.out.println(sum+"sum");
            nodeInfo.setRemainVolume(getRemainVolume(sum));
        }

    }

    private static String getRemainVolume(long len) {
        String s = nodeInfo.volume;
        String ss;
        float val;
        if (s.substring(s.length()-2,s.length()).equals("GB")){
            val = Float.parseFloat(s.substring(0,s.length()-2))*1024*1024*1024;
        }else {
            val = Float.parseFloat(s.substring(0,s.length()-2))*1024*1024;
        }
        val -= len;
        if (val/(1024*1024*1024)>1){
            ss = Float.toString(val/(1024*1024*1024))+"GB";
        }else {
            ss = Float.toString(val/(1024*1024))+"MB";
        }
        return ss;
    }

    private static void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                    sendpacket(0,listeningport);
                    System.out.println("发送了一个确认连接数据包");
                }
        },new Date(System.currentTimeMillis()+period),period);
    }

    private static void sendpacket(int type, int listeningport) {
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(listeningport);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(listeningport);
            dos.writeInt(type); //代表为正常的确认数据包
            byte[] buffer = bos.toByteArray();
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,
                    InetAddress.getLocalHost(),9999);
            datagramSocket.send(packet);
            datagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readinitfile() {
        try {
            Properties properties = new Properties();
            FileInputStream fis = new FileInputStream(new File("storage*.properties"));
            properties.load(fis);
            nodeInfo = new NodeInfo(properties);
        } catch (IOException e) {
            System.out.println("配置文件打开失败");
            System.exit(1);
            e.printStackTrace();
        }
    }
    private static void registertoFileServer(int listeningport){
        try {
            DatagramSocket datagramSocket = new DatagramSocket(nodeInfo.nodePort);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
            objectOutputStream.writeObject(nodeInfo);
            byte[] buffer = bos.toByteArray();
            System.out.println(buffer.length);
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,InetAddress.getLocalHost(),2222);
            datagramSocket.send(packet);
            datagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
