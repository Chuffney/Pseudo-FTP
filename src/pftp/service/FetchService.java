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
import java.nio.file.Paths;

public class FetchService {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int STEP_COUNT = 50;

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
            } else if (code == ResponseCode.OK.code) {
                byte[] byteArray = in.readNBytes(Long.BYTES);
                long fileSize = bytesToLong(byteArray);

                long fetchStart = System.currentTimeMillis();

                File file = Paths.get(filePath).getFileName().toFile();
                System.out.println("Transferring file to " + file.getName());
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    transfer(in, fos, fileSize);

                    long time = System.currentTimeMillis() - fetchStart;
                    System.out.println("time: " + (float) time / 1000f + " s");
                    System.out.println("size: " + fileSize / 1000 + " kB");
                    if (fileSize > 0 && time > 0) {
                        System.out.println("speed: " + (fileSize / time) + " kB / s");
                    }
                }
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
        long stepSize = size / STEP_COUNT;
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);

            if (stepSize > 0) {
                transferred += read;
                printProgressBar(transferred, stepSize);
            }
        }
        System.out.println();
    }

    private static void printProgressBar(long transferred, long stepSize) {
        int steps = (int) (transferred / stepSize);
        System.out.print('|');
        System.out.print("=".repeat(steps));
        System.out.print(" ".repeat(STEP_COUNT - steps));
        System.out.print('|');
        System.out.print('\r');
    }
}
