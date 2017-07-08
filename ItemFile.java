import java.io.Serializable;

/**
 * Created by chaomaer on 7/7/17.
 */
public class ItemFile implements Serializable{
    public String uuid;
    public String filename;
    public long filelen;
    public int port1;
    public int port2;

    public ItemFile(String uuid, String filename, long filelen, int port1, int port2) {
        this.uuid = uuid;
        this.filename = filename;
        this.filelen = filelen;
        this.port1 = port1;
        this.port2 = port2;
    }

    @Override
    public String toString() {
        return "ItemFile{" +
                "uuid='" + uuid + '\'' +
                ", filename='" + filename + '\'' +
                ", filelen=" + filelen +
                ", port1=" + port1 +
                ", port2=" + port2 +
                '}';
    }
}
