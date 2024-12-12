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

// Singleton class
public class socketHelper {
    private static socketHelper instance;
    private Socket serverFD;
    private OutputStream outputStream;
    private InputStream inputStream;

    private socketHelper() {}

    public static synchronized socketHelper getInstance() {
        if (instance == null) {
            instance = new socketHelper();
        }
        return instance;
    }

    public String IP;
    public int portNum;

    public void connect() throws IOException {
        try {
            serverFD = new Socket(IP, portNum);
            outputStream = serverFD.getOutputStream();
            inputStream = serverFD.getInputStream();
        } catch (IOException e) {
            throw new IOException("Failed to connect to server: " + e.getMessage(), e);
        }
    }

    public void close() throws IOException {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (serverFD != null && !serverFD.isClosed()) {
                serverFD.close();
            }
        } catch (IOException e) {
            throw new IOException("Error closing resources: " + e.getMessage(), e);
        }
    }

    public void sendInt(int num) throws IOException {
        byte[] data = ByteBuffer.allocate(Integer.BYTES).putInt(num).array();
        sendData(data);
    }

    public void sendFloat(float num) throws IOException {
        byte[] data = ByteBuffer.allocate(Float.BYTES).putFloat(num).array();
        sendData(data);
    }

    public void sendString(String str) throws IOException {
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        sendInt(data.length); // Send string length
        sendData(data); // Send string data
    }

    private void sendData(byte[] data) throws IOException {
        int offset = 0;
        while (offset < data.length) {
            int bytesToSend = Math.min(globals.CHUNCK_SIZE, data.length - offset);
            outputStream.write(data, offset, bytesToSend);
            offset += bytesToSend;
        }
        outputStream.flush(); // Ensure all data is sent
    }

    public void sendImg(Bitmap selectedImage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        // Send the length of the image first
        int length = imageBytes.length;
        sendInt(length);
        // Send the image data
        int bytesSent = 0;
        while (bytesSent < length) {
            int chunkSize = Math.min(globals.CHUNCK_SIZE, length - bytesSent); // Send in chunks of up to 4096 bytes
            outputStream.write(imageBytes, bytesSent, chunkSize);
            bytesSent += chunkSize;
        }
        outputStream.flush();
    }


    public int recvInt() throws IOException {
        byte[] revd = new byte[Integer.BYTES];
        readFully(revd);
        ByteBuffer byteBuffer = ByteBuffer.wrap(revd).order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt();
    }

    public float recvFloat() throws IOException {
        byte[] revd = new byte[Float.BYTES];
        readFully(revd);
        ByteBuffer byteBuffer = ByteBuffer.wrap(revd).order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getFloat();
    }

    public String recvString() throws IOException {
        int size = recvInt(); // Receive string size
        byte[] buffer = new byte[globals.CHUNCK_SIZE];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int totalBytesReceived = 0;

        while (totalBytesReceived < size) {
            int bytesRead = inputStream.read(buffer, 0, Math.min(buffer.length, size - totalBytesReceived));
            if (bytesRead == -1) throw new IOException("End of stream reached before receiving full data.");
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            totalBytesReceived += bytesRead;
        }

        return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()).trim(); // Convert to string and trim
    }

    public Bitmap recvImg() throws IOException {
        int imageSize = recvInt(); // Receive image size
        if (imageSize <= 0) {
            throw new IOException("Received invalid image size: " + imageSize);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[globals.CHUNCK_SIZE];
        int totalBytesReceived = 0;

        while (totalBytesReceived < imageSize) {
            int bytesRead = inputStream.read(buffer, 0, Math.min(buffer.length, imageSize - totalBytesReceived));
            if (bytesRead == -1) {
                throw new IOException("End of stream reached before receiving full image.");
            }
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            totalBytesReceived += bytesRead;
        }

        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        if (bitmap == null) {
            throw new IOException("Failed to decode received image.");
        }

        return bitmap;
    }




    private void readFully(byte[] buffer) throws IOException {
        int bytesReadTotal = 0;

        while (bytesReadTotal < buffer.length) {
            int bytesRead = inputStream.read(buffer, bytesReadTotal, buffer.length - bytesReadTotal);
            if (bytesRead == -1) throw new IOException("End of stream reached before reading full data.");
            bytesReadTotal += bytesRead;
        }
    }
}
