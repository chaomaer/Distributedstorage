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
        while (true) {
            String command = input.next();
            if (command.equals("exit")) {
                break;
            }
            if (!commandset.contains(command)) {
                System.out.println("命令输入有错误\n");
                continue;
            }
            String filename = input.next();
            try {
                Socket socket = new Socket(IP, 3333); //FileServer request
                Clientfun clientfun = new Clientfun(socket);
                switch (command) {
                    case "upload":
                        clientfun.upload(filename);
                        socket.close();
                        break;
                    case "download":
                        clientfun.download(filename);
                        socket.close();
                        break;
                    case "remove":
                        clientfun.remove(filename);
                        socket.close();
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
