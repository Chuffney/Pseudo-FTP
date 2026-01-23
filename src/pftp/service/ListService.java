package pftp.service;

import pftp.ArgumentParsing;
import pftp.model.Command;
import pftp.model.Param;
import pftp.model.ResponseCode;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ListService {
    public static void list() throws IOException {
        try (Socket clientSocket = ConnectionService.openSocket();
             InputStream in = clientSocket.getInputStream();
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            String targetDir = ArgumentParsing.getParamValue(Param.DIR);

            out.write(Command.LIST.code);
            out.write(targetDir);
            out.newLine();
            out.flush();

            int responseCode = (byte) in.read();
            if (ResponseCode.NOT_FOUND.code == responseCode) {
                System.out.println("Directory \"" + targetDir + "\" not found.");
                return;
            }

            byte[] bytes = in.readAllBytes();
            System.out.println(new String(bytes, StandardCharsets.UTF_8));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String listFiles(String prefix, File dir) {
        if (!dir.isDirectory()) return "\"" + dir + "\" is not a directory";
        return Arrays.stream(dir.listFiles()).map(f -> prefix + "/" + f.getName()).collect(Collectors.joining("\n"));
    }
}
