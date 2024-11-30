package com.kaaa.talabat_lite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
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
    public void sendImg(Bitmap selectedImage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        // Send the length of the image first
        int length = imageBytes.length;
        sendInt(length);
        // Send the image data
        int bytesSent = 0;
        while (bytesSent < length) {
            int chunkSize = Math.min(4096, length - bytesSent); // Send in chunks of up to 4096 bytes
            outputStream.write(imageBytes, bytesSent, chunkSize);
            bytesSent += chunkSize;
        }
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
    public String recvString() throws IOException {
        int size = recvInt();
        // Step 2: Prepare to receive the string in chunks
        byte[] buffer = new byte[1024]; // Set chunk size (adjustable)
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int totalBytesReceived = 0;
        // Step 3: Receive the string in chunks
        while (totalBytesReceived < size) {
            // Calculate how much data is remaining to be received
            int remaining = size - totalBytesReceived;
            int currentChunkSize = Math.min(buffer.length, remaining);

            // Read data into the buffer
            int bytesRead = inputStream.read(buffer, 0, currentChunkSize);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached before receiving full data.");
            }
            // Write the received chunk to the byte array output stream
            byteArrayOutputStream.write(buffer, 0, size);

            // Update total bytes received
            totalBytesReceived += size;
        }
        // Step 4: Convert the received byte array to a string
        String receivedString = byteArrayOutputStream.toString("UTF-8");

        // Close resources
        byteArrayOutputStream.close();
        return receivedString;
    }
    public Bitmap recvImg() throws IOException {
        // Convert the byte array to an integer (image size)
        int imageSize = recvInt();
        // Step 2: Read the image data in chunks
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int totalBytesReceived = 0;
        while (totalBytesReceived < imageSize) {
            int remaining = imageSize - totalBytesReceived;
            int currentChunkSize = Math.min(buffer.length, remaining);
            // Read the chunk of image data
            int bytesRead = inputStream.read(buffer, 0, currentChunkSize);
            if (bytesRead == -1) {
                throw new IOException("End of stream reached before receiving full data.");
            }
            // Write the chunk to the ByteArrayOutputStream
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            totalBytesReceived += bytesRead;
        }

        // Step 3: Convert the byte array to a Bitmap
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}