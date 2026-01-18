package pftp.service;

import pftp.ArgumentParsing;
import pftp.model.Command;
import pftp.model.Param;
import pftp.model.ResponseCode;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SendService {
    public static void send() throws IOException {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(ArgumentParsing.getParamIntValue(Param.PORT));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }

        System.out.println("awaiting requests on port " + ArgumentParsing.getParamValue(Param.PORT));

        File workingDir = new File(ArgumentParsing.getParamValue(Param.DIR));

        while (true) {
            Socket clientSocket = serverSocket.accept();
            InetAddress clientAddress = clientSocket.getInetAddress();
            System.out.print(clientAddress.getHostAddress() + ' ');

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();


            String request = in.readLine();
            char operation = request.charAt(0);
            String reqBody = request.substring(1);
            File file = new File(workingDir, reqBody);

            if (!file.exists()) {
                out.write(ResponseCode.NOT_FOUND.code);
                continue;
            } else if (!file.getCanonicalPath().startsWith(workingDir.getCanonicalPath())) {
                out.write(ResponseCode.FORBIDDEN.code);
                continue;
            }

            if (Command.LIST.code == operation) {
                System.out.println("LIST: " + reqBody);

                if (!file.isDirectory()) {
                    out.write(ResponseCode.NOT_FOUND.code);
                    continue;
                }

                String fileList = ListService.listFiles(reqBody, file);
                out.write(ResponseCode.OK.code);
                out.write(fileList.getBytes());
            } else if (Command.FETCH.code == operation) {
                System.out.println("FETCH: " + reqBody);

                if (file.isDirectory()) {
                    out.write(ResponseCode.NOT_FOUND.code);
                    continue;
                }

                out.write(ResponseCode.OK.code);
                out.write(longToBytes(file.length()));
                FileInputStream fis = new FileInputStream(file);
                fis.transferTo(out);
                fis.close();
            } else {
                System.out.println("UNKNOWN: " + operation);
                out.write(ResponseCode.BAD_REQUEST.code);
            }
            clientSocket.close();
        }
    }

    public static byte[] longToBytes(long l) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(l);
        return buffer.array();
    }
}
