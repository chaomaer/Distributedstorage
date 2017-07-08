import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Created by chaomaer on 7/6/17.
 */
public class FileClient {
    private static HashSet<String> commandset = new HashSet<>();
    private static final String IP = "127.0.0.1";
    public static void main(String[] args) {
        initcommandset();
        Scanner input = new Scanner(System.in);
        while (true){
            String command = input.next();
            if (command.equals("exit")){
                break;
            }
            if (!commandset.contains(command)){
                System.out.println("命令输入有错误\n");
                continue;
            }
            String parameter = input.next();
            try {
                Socket socket1 = new Socket(IP,3333); //FileServer request
                switch (command){
                    case "upload":
                        DataInputStream dataInputStream = new DataInputStream(socket1.getInputStream());
                        Utils.Requestupload(new File(parameter),socket1);
                        String uuid = dataInputStream.readUTF();
                        int port1 = dataInputStream.readInt();
                        int port2 = dataInputStream.readInt();
                        System.out.println(uuid+" port1:"+port1+" port2:"+port2);
                        socket1.close();
                        Socket socket2 = new Socket(IP,port1);
                        Utils.UploadtoStorage(new File(parameter),uuid,socket2,port2);
                        System.out.println("文件存储结束");
                        socket2.close();
                        break;
                    case "download":
                        // 首先要获取到主存储和备份存储的位置以及uuid对应的原文件名
                        Utils.RequestDownload(parameter,socket1);
                        ObjectInputStream objectInputStream = new ObjectInputStream(socket1.getInputStream());
                        ItemFile itemFile = (ItemFile) objectInputStream.readObject();
                        System.out.println(itemFile.toString());
                        Utils.DownloadFromStorage(itemFile);
                        break;
                    case "remove":
                        Utils.RequestDeleteFile(parameter,socket1);
                        ObjectInputStream objectInputStream1 = new ObjectInputStream(socket1.getInputStream());
                        ItemFile itemFile1 = (ItemFile) objectInputStream1.readObject();
                        System.out.println(itemFile1.toString());
                        Utils.DeleterFromStorage(itemFile1);
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void initcommandset() {
        commandset.add("upload");
        commandset.add("download");
        commandset.add("remove");
    }
}
