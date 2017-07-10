import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.UUID;

/**
 * Created by chaomaer on 7/6/17.
 */
public class FileopeThread extends Thread {
    public static Hashtable<String,ItemFile> table = new Hashtable<>();
    public static RegisterThread registerThread;
    public FileopeThread(RegisterThread thread){
        registerThread = thread;
    }
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(3333); // 用来监听FileClient端的信息
            while (true){
                Socket socket = ss.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            int command = dataInputStream.readInt();
                            switch (command){
                                case 0:
                                    parseupload(dataInputStream,dataOutputStream);
                                    socket.close();
                                    break;
                                case 1:
                                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(dataOutputStream);
                                    parseupdownload(dataInputStream,objectOutputStream);
                                    break;
                                case 2:
                                    ObjectOutputStream objectOutputStream1 = new ObjectOutputStream(dataOutputStream);
                                    parsedelete(dataInputStream,objectOutputStream1);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parsedelete(DataInputStream dataInputStream, ObjectOutputStream dataOutputStream) {
        parseupdownload(dataInputStream,dataOutputStream);
    }

    private void parseupdownload(DataInputStream dataInputStream, ObjectOutputStream objectOutputStream) {
        try {
            String key = dataInputStream.readUTF();
            objectOutputStream.writeObject(table.get(key));
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseupload(DataInputStream dataInputStream , DataOutputStream dataOutputStream){
        String filename = null;
        try {
            filename = dataInputStream.readUTF();
            String uuid = UUID.randomUUID().toString();
            long filelen = dataInputStream.readLong();
            System.out.println(filename+" "+filelen);
            String vals = SLB(registerThread.arrayList);
            int val1 = Integer.parseInt(vals.substring(0,vals.indexOf("&")));
            int val2 = Integer.parseInt(vals.substring(vals.indexOf("&")+1,vals.length()));
            // 填表
            table.put(uuid,new ItemFile(uuid,filename,filelen,val1,val2));
            dataOutputStream.writeUTF(uuid);
            dataOutputStream.writeInt(val1);
            dataOutputStream.writeInt(val2);
            dataOutputStream.flush();
            System.out.println("发送消息到FileClient端\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String SLB(ArrayList<NodeInfo> arrayList) {
        System.out.println("目前的服务器台数量:");
        System.out.println(arrayList.size());
        Random random = new Random();
        int val1 = random.nextInt(arrayList.size());
        int val2 = random.nextInt(arrayList.size());
        while (val2==val1){
            val2 = random.nextInt(arrayList.size());
        }
        val1 = arrayList.get(val1).NodePort;
        val2 = arrayList.get(val2).NodePort;
        System.out.println(val1+"&"+val2);
        return val1+"&"+val2;
    }
}