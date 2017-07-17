import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chaomaer on 7/12/17.
 */
public class FileMonitor extends JFrame {
    public  static ArrayList<String> tablearrayList = new ArrayList<>();
    // 是一个数据表的索引,非常之重要,数据的更细,删除都是由它进行
    public  static DefaultTableModel tableModel;   //表格模型对象
    public  static JTable table;
    public  static JScrollPane scrollPane;
    public FileMonitor()
    {
        super();
        setTitle("文件管理器");
        setBounds(100,100,900,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String[] columnNames = {"编号","文件原始名称","文件大小","主存储节点信息","备份节点信息"};   //列名
        tableModel = new DefaultTableModel(null,columnNames);
        table = new JTable(tableModel){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scrollPane = new JScrollPane(table);   //支持滚动
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(table);
        final JPanel panel = new JPanel();
        getContentPane().add(panel,BorderLayout.SOUTH);
        // 以上部分都为废话，就是为了写一个丑陋的GUI
    }
    public static void main(String[] args) {
        FileMonitor fileMonitor = new FileMonitor();
        fileMonitor.setVisible(true);
        startmonitor();
    }

    private static void startmonitor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(6666);
                    byte[] buffer = new byte[1024*1024];
                    while (true){
                        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                        socket.receive(packet);
                        ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        try {
                            ItemFile itemFile = (ItemFile) ois.readObject();
                            System.out.println(itemFile.toString());
                            display(itemFile);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    synchronized private static void display(ItemFile itemFile) {
        String[] ss = new String[5];
        ss[0] = itemFile.uuid;
        ss[1] = itemFile.filename;
        ss[2] = itemFile.filelen+"";
        ss[3] = itemFile.port1+"";
        ss[4] = itemFile.port2+"";
        // 在展示之前看看是要修改，增加，还是删除
        ArrayList<String> arrayList = tablearrayList ; // 第一个代表port，第二个代表索引
        if (!arrayList.contains(ss[0])){ // 增加
            tableModel.addRow(ss);
            arrayList.add(ss[0]);
            System.out.println("UUID"+"------------>"+ss[0]);
        }else{ //删除
            int index = -1;
            for (int i = 0; i < arrayList.size(); i++) {
                if (arrayList.get(i).equals(ss[0])){
                    index = i;
                    arrayList.remove(index);
                    tableModel.removeRow(index);
                    break;
                }
            }
            if (index == -1) System.out.println("NOT FIND");

        }
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.println(i+" "+arrayList.get(i));
        }
        scrollPane.invalidate();
    }
}
