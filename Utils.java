import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.zip.Deflater;

/**
 * Created by chaomaer on 7/6/17.
 */
public class Utils {
    private static byte[] buffer = new byte[256];
    // 用来发送文件信息到FileServer
    public static void Requestupload(File file, Socket socket) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(0); //type 代表类型为上传文件
            dataOutputStream.writeUTF(file.getName());// file name
            dataOutputStream.writeLong(file.length()); // file length
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 向服务器端发送要下载文件的uuid
    public static void RequestDownload(String uuid, Socket socket){
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(1); //type 代码类型为下载文件
            dataOutputStream.writeUTF(uuid);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void RequestDeleteFile(String uuid,Socket socket){
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(2); //type 代表类型为删除文件
            dataOutputStream.writeUTF(uuid);//只需要输入文件名
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 用来发送文件内容到StorageNode,同时把备份信息的端口号发送过去，其中端口信息放到最后
    public static void UploadtoStorage(File file,String uuid, Socket socket, int port2) {
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(0); //类型为1代表文件发送到主节点，需要携带备份信息
            dataOutputStream.writeUTF(uuid);
            dataOutputStream.writeLong(file.length());
            FileInputStream fis = new FileInputStream(file);
            int line;
            while ((line = fis.read()) != -1) {
                dataOutputStream.write(line);
            }
            dataOutputStream.writeInt(port2);  // 将备份的信息存到最后
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void DeleterFromStorage(ItemFile itemFile){
        int port1 = itemFile.port1;
        int port2 = itemFile.port2;
        SenddeletetoStorage(itemFile.uuid,port1);
        System.out.println("删除信息发送到"+port1);
        SenddeletetoStorage(itemFile.uuid,port2);
        System.out.println("删除信息发送到"+port2);
    }
    public static void SenddeletetoStorage(String filename,int port){
        try {
            Socket socket = new Socket("127.0.0.1",port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(3);
            dataOutputStream.writeUTF(filename);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void DownloadFromStorage(ItemFile itemFile){
        // 首先尝试到主服务器上下载文件
        try {
            File file = new File(itemFile.filename);
            Socket socket1 = new Socket("127.0.0.1",itemFile.port1);
            DataOutputStream dataOutputStream = new DataOutputStream(socket1.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket1.getInputStream());
            dataOutputStream.writeInt(2);  //代表要下载文件
            dataOutputStream.writeUTF(itemFile.uuid);
            dataOutputStream.flush();
            FileOutputStream fos = new FileOutputStream(file);
            long filelen = dataInputStream.readLong();
            long sum = 0;
            int line;
            System.out.println("正在下载文件中........");
            while (sum!=filelen){
                line = dataInputStream.read();
                fos.write(line);
                sum++;
            }
            System.out.println("恭喜,文件下载结束");
        } catch (IOException e) {
            System.out.println("主服务器出现异常");
            System.out.println("尝试从备份服务器下载文件");
            // 如果发生异常，尝试从备份文件下载
            try {
                File file = new File(itemFile.filename);
                Socket socket1 = new Socket("127.0.0.1",itemFile.port2);
                DataOutputStream dataOutputStream = new DataOutputStream(socket1.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket1.getInputStream());
                dataOutputStream.writeInt(2);  //代表要下载文件
                dataOutputStream.writeUTF(itemFile.uuid);
                dataOutputStream.flush();
                FileOutputStream fos = new FileOutputStream(file);
                long filelen = dataInputStream.readLong();
                long sum = 0;
                int line;
                System.out.println("正在下载文件中........");
                while (sum!=filelen){
                    line = dataInputStream.read();
                    fos.write(line);
                    sum++;
                }
                System.out.println("恭喜,文件下载结束");
            } catch (IOException e1) {
                System.out.println("主节点和备份节点都出错:)");
            }
        }

    }

    // 用来备份的文件，发送文件到指定的备份StorageNode
        public static void SendBackupfile(File file, Socket socket) {
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(1); //类型为1代表为备份文件，不需要携带备份端口信息
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.writeLong(file.length());
            FileInputStream fis = new FileInputStream(file);
            int line;
            while ((line = fis.read(buffer)) != -1) {
                dataOutputStream.write(buffer,0,line);
            }
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void FileBackup(String rootFolder, DataInputStream dataInputStream) {
        String filename = null;
        int port = -1;
        try {
            filename = dataInputStream.readUTF();
            System.out.println(filename);
            long len = dataInputStream.readLong();
            int line;
            File file = new File(rootFolder,filename);
            FileOutputStream fos = new FileOutputStream(file);
            System.out.println("开始备份文件");
            int sum = 0;
            while ((sum = dataInputStream.read(buffer))!=-1){
                fos.write(buffer,0,sum);
            }
            System.out.println("备份文件结束");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void StorageAndbackup(String rootFolder, DataInputStream dataInputStream) {
        String filename = null;
        int port = -1;
        try {
            filename = dataInputStream.readUTF();
            System.out.println(filename);
            long len = dataInputStream.readLong();
            File file = new File(rootFolder,filename);
            FileOutputStream fos = new FileOutputStream(file);
            System.out.println("开始写入文件");
            port = dataInputStream.readInt();
            System.out.println("对应的port"+port);
            int sum = 0;
            while ((sum = dataInputStream.read(buffer))!=-1){
                fos.write(buffer,0,sum);
            }
            System.out.println("写入文件完成...");
            System.out.println("开始写入备份文件...");
            // 开始备份信息
            Socket socket1 = new Socket("127.0.0.1",port);
            Utils.SendBackupfile(new File(rootFolder,filename),socket1);
            socket1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void FileDownload(String rootFolder, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        try {
            String uuid = dataInputStream.readUTF();
            File file = new File(rootFolder,uuid);
            int line;
            dataOutputStream.writeLong(file.length());
            FileInputStream fis = new FileInputStream(file);
            while ((line = fis.read(buffer))!=-1){
                dataOutputStream.write(buffer,0,line);
            }
            dataOutputStream.flush();
            System.out.println("写入的长度是"+dataOutputStream.size());
            System.out.println("运行的线程是"+Thread.currentThread().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendtoFileMonitor(ItemFile itemFile) {
        try {
            DatagramSocket socket = new DatagramSocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(itemFile);
            byte[] buffer = bos.toByteArray();
            // 端口6666负责接受NodeMonitor的显示工作
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length, InetAddress.getLocalHost(),6666);
            socket.send(packet);
            System.out.println("Packet"+packet.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendtoNodeMonitor(NodeInfo nodeInfo) {
        try {
            DatagramSocket socket = new DatagramSocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(nodeInfo);
            byte[] buffer = bos.toByteArray();
            // 端口6666负责接受NodeMonitor的显示工作
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,InetAddress.getLocalHost(),6667);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
