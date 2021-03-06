import java.io.*;
import java.security.*;
import java.util.UUID;
import java.util.zip.*;
import javax.crypto.*;


public class Tool {
    private static final String pkey = "O8XFAYT9JB4ETH87OXWF5Q06OEW6Z6EXLQKB4361U68AC4D93FX9KZ14Y2523BFTSF"
            + "52YF9U6W5AX34D42D1C8X8896H88E4C198E7EXEB080XHB5V95Z1K0JRA4BOFF94K621I6BBC8347SFFRFRF39C2C7V"
            + "JASEC24B48L0F4IB1K9B8FBTMRH9CS80EN6N68X71U64V834GQ6FCXT7Z375FS9OI3HBM6F2FFWH0AF5CC51FE20BN1"
            + "2792B2R51G44C75334AV1168I9OB07AAWIO31BABX4G98A22AB7TA5EZE4B4REYCPF53C4A6R34RK5O8774D3QJBC5G"
            + "27Y4V3BH2CUET71B9W24N11N45A8B92V6X69VVD37LYG9CUW614B7U9D32F3352F26R075I7VH9CL7V6EX0AN557EBY"
            + "G4O9M5UQ62CJ83AVF162EZZV4S1O80ZP437163BPE631ECCCB0FK55223ZMC639E29940P6O31HGEEIE9M8B0ADB1H7"
            + "G9NEQ1CG8DAY13J1C1Q731C7HZVAC1LMAR15EUDAP2IYNA9KH2KYLAQFBE3Q3539D1M5OAJ2F4BAFL70SEQYBSCBQD3"
            + "BFAC64SD04PSGW06XF1ADARYDB93105116DB59I2B34Q6IE948P2349SPHBA8Z8F081VB04FJUZ80JHUPX54BCE58D4"
            + "P81357A8RWAMN44DY8A49581AWBVAB7B2AKVN1SUB1B8NB4I6B5439VB54FBH9FFFFZCO804F8MF4912LD87ZAVBJSW"
            + "FLFHR5189597X5DBR9DF5Q83E7Q1T62ND0DZQF67PB83DELQ6MQGXSUZLL3J6JY0TF41JIXRKAO245V515R38G373Z1"
            + "N30WVPOB481QDQEGEJ9W883R3C242RYLZ8RD3IT31D7H1FE9VW9X9ADK3QU1BC5HS05B77P8A90B5RC7RK03PW7BX4N"
            + "2C6F1FA9146ZE5Y8BAAT3B4SF04F71OH5D5C4ARCD7D70N74";
    private static final String aeskey = "ACED00057372001F6A617661782E63727970746F2E737065632E5365637265744"
            + "B6579537065635B470B66E230614D0200024C0009616C676F726974686D7400124C6A6176612F6C616E672F53747"
            + "2696E673B5B00036B65797400025B427870740003414553757200025B42ACF317F8060854E002000078700000001"
            + "05F226F6452F56B28FEB96947587F6787";
    private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;
    private static final String TEMPCOMPRESSFILE = "tempcom.txt";
    private static final String TEMPENCRYPTFILE = "tempenc.txt";
    private static final String TEMPEDCRYPTFILE = "tempdecry.txt";
    private static final String TEMPEDCOMPRTFILE = "tempdecom.txt";
    private static final byte[] buffer = new byte[MAX_BUFFER_SIZE];
    public static byte[] convertHexToByteArray(String hexString) {
        if (hexString == null || hexString.length() == 0) {
            return new byte[0];
        } else {
            if (hexString.length() % 2 == 1) {
                throw new IllegalArgumentException("hex string length is odd number");
            }
            hexString = hexString.toUpperCase();
            byte[] buffer = new byte[hexString.length() / 2];
            int i1 = 0;
            int i2 = 0;

            for (int i = 0; i < hexString.length(); ) {
                char c1 = hexString.charAt(i);
                char c2 = hexString.charAt(i + 1);
                if (c1 >= '0' && c1 <= '9') {
                    i1 = c1 - '0';
                } else {
                    i1 = c1 - 'A' + 10;
                }
                if (c2 >= '0' && c2 <= '9') {
                    i2 = c2 - '0';
                } else {
                    i2 = c2 - 'A' + 10;
                }
                buffer[i / 2] = (byte) (i1 * 16 + i2);
                i = i + 2;
            }
            return buffer;
        }
    }


    public static byte[] toByteArray(String data) {
        if (data == null || data.length() == 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[data.length()];
        char[] chars = data.toCharArray();

        for (int i = 0; i != chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }

        return bytes;
    }

    public static Object deserialize(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] compress(byte[] input) {
        byte[] output = new byte[MAX_BUFFER_SIZE];
        // Deflater compresser = new Deflater(Deflater.BEST_SPEED, true);
        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION, true);

        compresser.setInput(input);
        compresser.finish();

        int len = compresser.deflate(output);
        compresser.end();

        byte[] result = new byte[len];
        System.arraycopy(output, 0, result, 0, len);
        return result;
    }

    public static byte[] decompress(byte[] input) {
        try {
            Inflater decompresser = new Inflater(true);
            decompresser.setInput(input, 0, input.length);
            byte[] output = new byte[MAX_BUFFER_SIZE];
            int len = decompresser.inflate(output);
            decompresser.end();
            byte[] result = new byte[len];
            System.arraycopy(output, 0, result, 0, len);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private static Key getKey() {
        Key key = (Key) deserialize(convertHexToByteArray(aeskey));
        return key;
    }

    public static byte[] encrypt(byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] e1 = cipher.doFinal(input);
            byte[] k1 = toByteArray(pkey);

            for (int i = 0; i < e1.length; i++) {
                e1[i] = (byte) (e1[i] ^ k1[(i + e1.length) % k1.length]);
                e1[i] = (byte) (e1[i] >> 4 & 0x0F | e1[i] << 4 & 0xF0);
            }
            return e1;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] decrypt(byte[] input) {
        try {
            byte[] k1 = toByteArray(pkey);
            for (int i = 0; i < input.length; i++) {
                input[i] = (byte) (input[i] >> 4 & 0x0F | input[i] << 4 & 0xF0);
                input[i] = (byte) (input[i] ^ k1[(i + input.length) % k1.length]);
            }

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getKey());

            return cipher.doFinal(input);
        } catch (Exception e) {
            return null;
        }
    }
    public static File getCompressFile(File file){
        return getCompressFile(file,TEMPCOMPRESSFILE);
    }
    public static File getCompressFile(File file,String filename){
        File file1 = new File(filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file1));
            int count;
            byte[] bytes;
            while ((count = fis.read(buffer)) != -1){
                if (count != MAX_BUFFER_SIZE){
                    bytes = new byte[count];
                    for (int i = 0; i <count ; i++) {
                        bytes[i] = buffer[i];
                    }
                }else {
                    bytes = buffer;
                }
                byte [] res = Tool.compress(bytes);
                dataOutputStream.writeInt(res.length);
                dataOutputStream.write(res);
            }
            dataOutputStream.writeInt(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("压缩后"+file1.length());
        return file1;
    }
    public static File getEncryptfile(File file){
        return getEncryptfile(file,TEMPENCRYPTFILE);
    }
    public static File getEncryptfile(File file,String filename){
        FileInputStream fis = null;
        File file1 = new File(filename);
        try {
            fis = new FileInputStream(file);
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(TEMPENCRYPTFILE));
            int line;
            byte[] bytes;
            while ((line = fis.read(buffer)) != -1){
                if (line<MAX_BUFFER_SIZE){
                    bytes = new byte[line];
                    for (int i = 0; i < line; i++) {
                        bytes[i] = buffer[i];
                    }
                }else {
                    bytes = buffer;
                }
                byte [] res = Tool.encrypt(bytes);
                dos.writeInt(res.length);
                dos.write(res);
            }
            dos.writeInt(0);
            fis.close();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("加密后"+file1.length());
        return file1;
    }
    public static File getDecryptfile(File file){
        return getDecryptfile(file, UUID.randomUUID().toString());
    }
    public static File getDecryptfile(File file,String filename){
        DataInputStream dis = null;
        File file1 = new File(filename);
        try {
            dis = new DataInputStream(new FileInputStream(file));
            FileOutputStream fos = new FileOutputStream(file1);
            int line;
            while ((line = dis.readInt())!=0){
                byte[] bytes = new byte[line];
                dis.read(bytes);
                fos.write(Tool.decrypt(bytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("解密后"+file1.length());
        return file1;
    }
    public static File getDecompfile(File file){
        return getDecompfile(file,TEMPEDCOMPRTFILE);
    }
    public static File getDecompfile(File file,String filename){
        DataInputStream dis = null;
        File file1 = new File(filename);
        try {
            dis = new DataInputStream(new FileInputStream(file));
            FileOutputStream fos = new FileOutputStream(file1);
            int line;
            while ((line = dis.readInt())!=0){
                byte[] bytes = new byte[line];
                int sum = dis.read(bytes);
                byte[] res = Tool.decompress(bytes);
                assert res != null;
                System.out.println(line+"---"+sum);
                fos.write(res);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("解压后"+file1.length());
        return file1;
    }
}
