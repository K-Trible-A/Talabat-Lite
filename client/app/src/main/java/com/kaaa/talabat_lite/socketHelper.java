package com.kaaa.talabat_lite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

// singleton class
public class socketHelper {
    private static socketHelper instance;
    private socketHelper() {}

    public static socketHelper getInstance() {
        if(instance == null){
            instance = new socketHelper();
        }
        return instance;
    }

    public String IP;
    public int portNum;
    private Socket serverFD;
    private OutputStream outputStream;
    private InputStream inputStream;

    public void connect() throws IOException {
        serverFD = new Socket(IP, portNum);
        outputStream = serverFD.getOutputStream();
        inputStream = serverFD.getInputStream();
    }
    public void close() throws IOException {
        serverFD.close();
        outputStream.close();
        inputStream.close();
    }

    public void sendInt(int num) throws IOException {
        byte[] Data = ByteBuffer.allocate(Integer.BYTES).putInt(num).array();
        outputStream.write(Data);
        outputStream.flush();
    }
    public void sendFloat(float num) throws IOException {
        byte[] Data = ByteBuffer.allocate(Float.BYTES).putFloat(num).array();
        outputStream.write(Data);
        outputStream.flush();
    }
    public void sendString(String str) throws IOException{
        // Sending string size first
        int sz = str.length();
        sendInt(sz);
        // Sending the string
        byte[] Data = str.getBytes(StandardCharsets.UTF_8);
        outputStream.write(Data);
        outputStream.flush();
    }

    public int recvInt() throws IOException{
        byte [] revd = new byte[Integer.BYTES];
        inputStream.read(revd);
        ByteBuffer byteBuffer = ByteBuffer.wrap(revd);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt();
    }
    public float recvFloat() throws IOException{
        byte[] revd = new byte[Float.BYTES];
        inputStream.read(revd);
        ByteBuffer byteBuffer = ByteBuffer.wrap(revd);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getFloat();
    }
    public String recvString() throws IOException{
        int sz = recvInt();
        byte[] revd = new byte[sz*Character.BYTES];
        inputStream.read(revd);
        return new String(revd).trim();
    }
}
