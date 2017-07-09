import java.io.Serializable;
import java.util.Properties;

/**
 * Created by chaomaer on 7/8/17.
 */
public class NodeInfo implements Serializable{
    public String NodeName;
    public String NodeIP;
    public int NodePort;
    public String RootFolder;
    public String Volume;
    public String FileServerIP;
    public int FileServerPort;
    public NodeInfo(Properties properties){
        NodeName = properties.getProperty("NodeName");
        NodeIP = properties.getProperty("NodeIP");
        NodePort = Integer.parseInt(properties.getProperty("NodePort"));
        RootFolder = properties.getProperty("RootFolder");
        Volume = properties.getProperty("Volume");
        FileServerIP = "127.0.0.1";
        FileServerPort = 2222;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "NodeName='" + NodeName + '\'' +
                ", NodeIP='" + NodeIP + '\'' +
                ", NodePort=" + NodePort +
                ", RootFolder='" + RootFolder + '\'' +
                ", Volume='" + Volume + '\'' +
                ", FileServerIP='" + FileServerIP + '\'' +
                ", FileServerPort=" + FileServerPort +
                '}';
    }
}
