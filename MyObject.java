import java.io.*;

public class MyObject implements Serializable{
    byte[] bytes;
    public void setData(byte[] bytearray){
        bytes = bytearray;
    }
    public byte[] getData(){
        return bytes;
    }
}
