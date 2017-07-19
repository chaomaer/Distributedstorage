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

    // 用来备份的文件，发送文件到指定的备份StorageNode
    synchronized public static void SendBackupfile(File file, Socket socket) {
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(1); //类型为1代表为备份文件，不需要携带备份端口信息
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.writeLong(file.length());
            FileInputStream fis = new FileInputStream(file);
            int line;
            while ((line = fis.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, line);
            }
            dataOutputStream.close();
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static void FileBackup(String rootFolder, DataInputStream dataInputStream) {
        String filename = null;
        int port = -1;
        try {
            filename = dataInputStream.readUTF();
            System.out.println(filename);
            long len = dataInputStream.readLong();
            int line;
            File file = new File(rootFolder, filename);
            FileOutputStream fos = new FileOutputStream(file);
            System.out.println("开始备份文件");
            int sum = 0;
            while ((sum = dataInputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, sum);
            }
            fos.close();
            System.out.println("备份文件结束");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static void StorageAndbackup(String rootFolder, DataInputStream dataInputStream) {
        String filename = null;
        int port = -1;
        try {
            filename = dataInputStream.readUTF();
            System.out.println(filename);
            long len = dataInputStream.readLong();
            File file = new File(rootFolder, filename);
            FileOutputStream fos = new FileOutputStream(file);
            System.out.println("开始写入文件");
            port = dataInputStream.readInt();
            System.out.println("对应的port" + port);
            int sum = 0;
            while ((sum = dataInputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, sum);
            }
            fos.close();
            System.out.println("流关系Hello");
            System.out.println("写入文件完成...");
            // 开始备份信息
            Socket socket1 = new Socket("127.0.0.1", port);
            Utils.SendBackupfile(new File(rootFolder, filename), socket1);
            socket1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static void FileDownload(String rootFolder, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        try {
            String uuid = dataInputStream.readUTF();
            File file = new File(rootFolder, uuid);
            int line;
            dataOutputStream.writeLong(file.length());
            FileInputStream fis = new FileInputStream(file);
            while ((line = fis.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, line);
            }
            fis.close();
            dataOutputStream.flush();
            System.out.println("写入的长度是" + dataOutputStream.size());
            System.out.println("运行的线程是" + Thread.currentThread().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendtoFileMonitor(ItemFile itemFile) {
        try {
            DatagramSocket socket = new DatagramSocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(itemFile);
            byte[] buffer = bos.toByteArray();
            // 端口6666负责接受NodeMonitor的显示工作
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), 6666);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendtoNodeMonitor(NodeInfo nodeInfo) {
        try {
            DatagramSocket socket = new DatagramSocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(nodeInfo);
            byte[] buffer = bos.toByteArray();
            // 端口6666负责接受NodeMonitor的显示工作
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), 6667);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
