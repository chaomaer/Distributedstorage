import java.io.*;
import java.net.*;
import java.util.Properties;

/**
 * Created by chaomaer on 7/6/17.
 */
public class StorageNode {
    public static NodeInfo nodeInfo;
    public static int listeningport;
    public static void main(String[] args) {
        readinitfile();
        listeningport = nodeInfo.NodePort;
        System.out.println("storageNode is ready in port "+listeningport);
        registertoFileServer2(listeningport);
        try {
            ServerSocket serverSocket = new ServerSocket(listeningport);
//            registertoFileServer(listeningport);
            while (true){
                Socket socket = serverSocket.accept();
                String ss;
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                int command = dataInputStream.readInt();
                switch (command){
                    case 0://代表存储文件，同时发送备份文件
                        Utils.StorageAndbackup(dataInputStream);
                        break;
                    case 1://代表为备份文件，需要在本主机进行备份
                        Utils.FileBackup(dataInputStream);
                        break;
                    case 2:
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        Utils.FileDownload(dataInputStream,dataOutputStream);
                        break;
                    case 3:
                        String filename = dataInputStream.readUTF();
                        System.out.println(filename);
                        File file = new File(filename);
                        file.delete();
                        break;
                }
            }
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
    // 注意文档要求使用UDP进行注册StorageNode到FileServer
    private static void registertoFileServer(int listeningport) {
        try {
            Socket socket = new Socket("127.0.0.1",2222);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(listeningport);
            int val = dataInputStream.readInt();
            if (val==1)
                System.out.println("connect to FileServer");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void registertoFileServer2(int listeningport){
        try {
            DatagramSocket datagramSocket = new DatagramSocket(nodeInfo.NodePort);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
            objectOutputStream.writeObject(nodeInfo);
            byte[] buffer = bos.toByteArray();
            System.out.println(buffer.length);
            System.out.println(buffer.length);
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,InetAddress.getLocalHost(),2222);
            datagramSocket.send(packet);
            datagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
