import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by chaomaer on 7/6/17.
 */
public class StorageNode {
    public static void main(String[] args) {
        System.out.println("input the port this process is listening:");
        int listeningport = new Scanner(System.in).nextInt();
        try {
            ServerSocket serverSocket = new ServerSocket(listeningport);
            registertoFileServer(listeningport);
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
}
