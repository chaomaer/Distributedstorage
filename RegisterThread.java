import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by chaomaer on 7/6/17.
 */
public class RegisterThread extends Thread {
    public ArrayList<Integer> arrayList = new ArrayList<>(); // 用来存储注册的各个StotageNode
    @Override
    public void run() {
            try {
                ServerSocket ss = new ServerSocket(2222); // 用来监听StotageNode的注册信息
                while (true){
                    Socket socket = ss.accept();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                int val = dataInputStream.readInt();
                                arrayList.add(val);
                                dataOutputStream.writeInt(1);
                                dataOutputStream.flush();
                                System.out.println(val+" "+"注册完毕");
                                socket.close();
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
}
