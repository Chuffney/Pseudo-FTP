package pftp.service;

import pftp.ArgumentParsing;
import pftp.model.Command;
import pftp.model.Param;
import pftp.model.ResponseCode;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class FetchService {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int STEP_SIZE = 50;

    public static void fetch() throws IOException {
        try (Socket clientSocket = ConnectionService.openSocket();
             InputStream in = clientSocket.getInputStream();
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            String filePath = ArgumentParsing.getParamValue(Param.FILE_PATH);
            out.write(Command.FETCH.code);
            out.write(filePath);
            out.newLine();
            out.flush();

            System.out.println("sent request");

            byte code = (byte) in.read();

            if (code == ResponseCode.NOT_FOUND.code) {
                System.out.println("File not found!");
            } else if (code == ResponseCode.FORBIDDEN.code) {
                System.out.println("Access denied!");
            } else if (code == ResponseCode.OK.code) {
                byte[] byteArray = in.readNBytes(Long.BYTES);
                long fileSize = bytesToLong(byteArray);

                long fetchStart = System.currentTimeMillis();

                File file = new File(new File(filePath).getName());
                System.out.println("Transferring file to " + file.getName());
                FileOutputStream fos = new FileOutputStream(file);
                transfer(in, fos, fileSize);

                long time = System.currentTimeMillis() - fetchStart;
                System.out.println("time: " + (float)time/1000f + " s");
                System.out.println("size: " + fileSize/1000 + " kB");
                System.out.println("speed: " + (fileSize / time) + " kB / s");

                fos.close();
            }
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        }
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    public static void transfer(InputStream in, OutputStream out, long size) throws IOException {
        Objects.requireNonNull(out, "out");
        long step = size/STEP_SIZE;
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        if (step > 0) {
            System.out.print("_".repeat(STEP_SIZE));
            System.out.println();
        }

        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
            if (step > 0 && transferred > step) {
                System.out.print('+');
                transferred %= step;
            }
        }
        System.out.println();
    }
}
