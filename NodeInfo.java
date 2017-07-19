import java.io.Serializable;
import java.util.Properties;

/**
 * Created by chaomaer on 7/8/17.
 */
public class NodeInfo implements Serializable{
    public String nodeName;
    public String nodeIP;
    public int nodePort;
    public String rootFolder;
    public String volume;
    public String fileServerIP;
    public int fileServerPort;
    public long starttime;
    // 文件的可用数量
    public int filenum;
    public String remainVolume;
    //用掉的容量大小
    // 再传递之前调用一下set方法
    public boolean canUse;
    public void setFilenum(int filenum) {
        this.filenum = filenum;
    }

    public void setRemainVolume(String remainVolume) {
        this.remainVolume = remainVolume;
    }
    public NodeInfo(Properties properties){
        nodeName = properties.getProperty("NodeName");
        nodeIP = properties.getProperty("NodeIP");
        nodePort = Integer.parseInt(properties.getProperty("NodePort"));
        rootFolder = properties.getProperty("RootFolder");
        volume = properties.getProperty("Volume");
        fileServerIP = "127.0.0.1";
        fileServerPort = 2222;
        starttime = System.currentTimeMillis();
        canUse = true;
    }

    @Override
    public String toString() {
        return
                "nodeName='" + nodeName + '\'' +
                ", nodeIP='" + nodeIP + '\'' +
                ", nodePort=" + nodePort +
                ", rootFolder='" + rootFolder + '\'' +
                ", volume='" + volume + '\'' +
                ", fileServerIP='" + fileServerIP + '\'' +
                ", fileServerPort=" + fileServerPort +
                ", starttime=" + starttime +
                ", filenum=" + filenum +
                ", remainVolume='" + remainVolume + '\'' +
                ", canUse=" + canUse;
    }
}
