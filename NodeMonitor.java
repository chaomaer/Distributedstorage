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
public class NodeMonitor extends JFrame {
    public  static ArrayList<String> tablearrayList = new ArrayList<>();
    // 是一个数据表的索引,非常之重要,数据的更细,删除都是由它进行
    public  static DefaultTableModel tableModel;   //表格模型对象
    public  static JTable table;
    public NodeMonitor()
    {
        super();
        setTitle("存储节点管理器");
        setBounds(100,100,900,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String[] columnNames = {"名称","ip","端口","实际容量","剩余量","文件数量","是否可用"};   //列名
        tableModel = new DefaultTableModel(null,columnNames);
        table = new JTable(tableModel){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scrollPane = new JScrollPane(table);   //支持滚动
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(table);
        final JPanel panel = new JPanel();
        getContentPane().add(panel,BorderLayout.SOUTH);
        // 以上部分都为废话，就是为了写一个丑陋的GUI
    }
    public static void main(String[] args) {
        NodeMonitor nodeMonitor = new NodeMonitor();
        nodeMonitor.setVisible(true);
        startmonitor();
    }

    private static void startmonitor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(6667);
                    byte[] buffer = new byte[1024*1024];
                    while (true){
                        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                        socket.receive(packet);
                        ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        try {
                            NodeInfo nodeInfo = (NodeInfo) ois.readObject();
                            System.out.println(nodeInfo.toString());
                            display(nodeInfo);
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
    synchronized private static void display(NodeInfo nodeInfo) {
        String[] ss = new String[7];
        ss[0] = nodeInfo.nodeName;
        ss[1] = nodeInfo.nodeIP;
        ss[2] = nodeInfo.nodePort+"";
        ss[3] = nodeInfo.volume;
        ss[4] = nodeInfo.remainVolume;
        ss[5] = nodeInfo.filenum+"";
        ss[6] = nodeInfo.canUse ?"true":"false";
        // 在展示之前看看是要修改，增加，还是删除
        ArrayList<String> arrayList = tablearrayList ; //
        if (!arrayList.contains(ss[0])){ // 增加
            tableModel.addRow(ss);
            arrayList.add(ss[0]);
            System.out.println(ss[0]);
        }else{ //修改
            int index = 0;
            for (int i = 0; i < arrayList.size(); i++) {
                if (arrayList.get(i).equals(ss[0])){
                    index = i;
                    break;
                }
            }
            for (int i = 1; i < 7; i++) {
                tableModel.setValueAt(ss[i],index,i);
            }
        }
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.println(i+" "+arrayList.get(i));
        }
    }
}
