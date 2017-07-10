import java.io.*;
import java.net.Socket;

/**
 * Created by chaomaer on 7/9/17.
 */
public class Clientfun {
    private Socket socket;
    private static final String IP = "127.0.0.1";

    public Clientfun(Socket socket) {
        this.socket = socket;
    }

    public void upload(String filename) {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            Requestupload(new File(filename), socket);
            String uuid = dataInputStream.readUTF();
            int port1 = dataInputStream.readInt();
            int port2 = dataInputStream.readInt();
            System.out.println(uuid + " port1:" + port1 + " port2:" + port2);
            Socket socket2 = new Socket(IP, port1);
            UploadtoStorage(new File(filename), uuid, socket2, port2);
            System.out.println("文件存储结束");
            socket2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void download(String filename) {
        RequestDownload(filename, socket);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ItemFile itemFile = null;
            try {
                itemFile = (ItemFile) objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            assert itemFile != null;
            System.out.println(itemFile.toString());
            DownloadFromStorage(itemFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(String filename) {
        RequestDeleteFile(filename, socket);
        ObjectInputStream objectInputStream1 = null;
        try {
            objectInputStream1 = new ObjectInputStream(socket.getInputStream());
            ItemFile itemFile1 = null;
            try {
                itemFile1 = (ItemFile) objectInputStream1.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println(itemFile1.toString());
            DeleterFromStorage(itemFile1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void Requestupload(File file, Socket socket) {
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

    public void UploadtoStorage(File file, String uuid, Socket socket, int port2) {
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

    public void RequestDownload(String uuid, Socket socket) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(1); //type 代码类型为下载文件
            dataOutputStream.writeUTF(uuid);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void DownloadFromStorage(ItemFile itemFile) {
        // 首先尝试到主服务器上下载文件
        try {
            File file = new File(itemFile.filename);
            Socket socket1 = new Socket(IP, itemFile.port1);
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
            while (sum != filelen) {
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
                Socket socket1 = new Socket(IP, itemFile.port2);
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
                while (sum != filelen) {
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
    public void RequestDeleteFile(String uuid,Socket socket){
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(2); //type 代表类型为删除文件
            dataOutputStream.writeUTF(uuid);//只需要输入文件名
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void DeleterFromStorage(ItemFile itemFile){
        int port1 = itemFile.port1;
        int port2 = itemFile.port2;
        SenddeletetoStorage(itemFile.uuid,port1);
        System.out.println("删除信息发送到"+port1);
        SenddeletetoStorage(itemFile.uuid,port2);
        System.out.println("删除信息发送到"+port2);
    }
    public void SenddeletetoStorage(String filename,int port){
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
}
