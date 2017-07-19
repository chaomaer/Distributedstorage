import java.io.*;
import java.net.Socket;

/**
 * Created by chaomaer on 7/9/17.
 */
public class Clientfun {
    private Socket socket;
    private static final String IP = "127.0.0.1";
    private byte[] buffer = new byte[1024*1024];

    public Clientfun(Socket socket) {
        this.socket = socket;
    }

    public void upload(String filename) {
        try {
            // 首先检查文件的合法性
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("文件不存在!");
                return;
            }
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            File file1 = RequestuploadtoFileSer(file, socket);
            String uuid = dataInputStream.readUTF();
            if (uuid.equals("empty")){
                System.out.println("存储服务器出现异常");
            }else{
                int port1 = dataInputStream.readInt();
                int port2 = dataInputStream.readInt();
                System.out.println(uuid + " port1:" + port1 + " port2:" + port2);
                Socket socket2 = new Socket(IP, port1);
                RequestUploadtoStorage(file1, uuid, socket2, port2);
                System.out.println("文件存储结束");
                socket2.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void download(String filename) {
        RequestDownloadFromFileSer(filename, socket);
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            ItemFile itemFile = null;
            int val = dataInputStream.readInt();
            if (val == 0){
                System.out.println("要下载的文件不存在");
                return;
            }
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(dataInputStream);
                itemFile = (ItemFile) objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            assert itemFile != null;
            RequestDownloadFromStorage(itemFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(String filename) {
        RequestDeleteFileFromSer(filename, socket);
        ObjectInputStream objectInputStream1 = null;
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int val = dataInputStream.readInt();
            if (val==0){
                System.out.println("要删除的文件不存在");
                return;
            }
            ItemFile itemFile1 = null;
            try {
                objectInputStream1 = new ObjectInputStream(dataInputStream);
                itemFile1 = (ItemFile) objectInputStream1.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println(itemFile1.toString());
            RequestDeleterFromStorage(itemFile1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File RequestuploadtoFileSer(File file, Socket socket) {
        File file1 = null;
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(0); //type 代表类型为上传文件
            dataOutputStream.writeUTF(file.getName());// file name
            file1 = getCandEfile(file); // 发送最终压缩和加密之后的文件大小
            dataOutputStream.writeLong(file1.length()); // file length
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file1;
    }

    private File getCandEfile(File file) {
        File file1 = ZipUtil.zip(file.getName());
        File file2 = Tool.getEncryptfile(file1);
        long len = file2.length();
        if (file1.delete()){
            System.out.println("文件1删除");
        }
        System.out.println("传上去的文件大小"+len);
        return file2;
    }

    public void RequestUploadtoStorage(File file, String uuid, Socket socket, int port2) {
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(0); //类型为1代表文件发送到主节点，需要携带备份信息
            dataOutputStream.writeUTF(uuid);
            dataOutputStream.writeLong(file.length());
            dataOutputStream.writeInt(port2);
            FileInputStream fis = new FileInputStream(file);
            int line;
            System.out.println("文件正在上传");
            long time1 = System.currentTimeMillis();
            while ((line = fis.read(buffer)) != -1) {
                dataOutputStream.write(buffer,0,line);
            }
            long time2 = System.currentTimeMillis();
            System.out.println("用时间"+((time2-time1)/1000)+"s");
            dataOutputStream.flush();
            fis.close();
            System.out.println("文件结束");
            if (file.delete()){
                System.out.println("文件2删除");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RequestDownloadFromFileSer(String uuid, Socket socket) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(1); //type 代码类型为下载文件
            dataOutputStream.writeUTF(uuid);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RequestDownloadFromStorage(ItemFile itemFile) {
        // 首先尝试到主服务器上下载文件
        try {
            File file = new File("temp.txt");
            Socket socket1 = new Socket(IP, itemFile.port1);
            DataOutputStream dataOutputStream = new DataOutputStream(socket1.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket1.getInputStream());
            dataOutputStream.writeInt(2);  //代表要下载文件
            dataOutputStream.writeUTF(itemFile.uuid);
            dataOutputStream.flush();
            FileOutputStream fos = new FileOutputStream(file);
            long filelen = dataInputStream.readLong();
            int sum = 0;
            System.out.println("正在下载文件中........");
            int a = 0;
            while ((sum = dataInputStream.read(buffer))!=-1){
                a += sum;
                fos.write(buffer,0,sum);
                fos.flush();
                if (a == filelen) break;
            }
            System.out.println("a"+a);
            if (file.length()!=filelen){
                System.out.println("文件下载中断");
                downloadfromBack(itemFile);
            }else {
                System.out.println(file.length()+"文件的长度");
                System.out.println("正在解压缩和解密...");
                File file1 = Tool.getDecryptfile(file);
                ZipUtil.unzip(file1.getName(),itemFile.filename);
                file.delete();
                file1.delete();
                socket1.close();
                System.out.println("恭喜,文件下载结束");
            }
        } catch (IOException e) {
                downloadfromBack(itemFile);
        }
    }

    private void downloadfromBack(ItemFile itemFile) {
        System.out.println("主服务器出现异常");
        System.out.println("尝试从备份服务器下载文件");
        // 如果发生异常，尝试从备份文件下载
        try {
            File file = new File("temp.txt");
            Socket socket1 = new Socket(IP, itemFile.port2);
            DataOutputStream dataOutputStream = new DataOutputStream(socket1.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket1.getInputStream());
            dataOutputStream.writeInt(2);  //代表要下载文件
            dataOutputStream.writeUTF(itemFile.uuid);
            dataOutputStream.flush();
            FileOutputStream fos = new FileOutputStream(file);
            long filelen = dataInputStream.readLong();
            int sum = 0;
            System.out.println("正在下载文件中........");
            int a = 0;
            while ((sum = dataInputStream.read(buffer))!=-1){
                a += sum;
                fos.write(buffer,0,sum);
                fos.flush();
                if (a == filelen) break;
            }
            System.out.println(file.length()+"文件的长度");
            System.out.println("正在解压缩和解密...");
            File file1 = Tool.getDecryptfile(file);
            ZipUtil.unzip(file1.getName(),itemFile.filename);
            file.delete();
            file1.delete();
            socket1.close();
            System.out.println("恭喜,文件下载结束");
            System.out.println("我是从备份文件下载下来的");
        } catch (IOException e1) {
            System.out.println("主节点和备份节点都出错:)");
        }
    }

    public void RequestDeleteFileFromSer(String uuid,Socket socket){
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(2); //type 代表类型为删除文件
            dataOutputStream.writeUTF(uuid);//只需要输入文件名
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void RequestDeleterFromStorage(ItemFile itemFile){
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
