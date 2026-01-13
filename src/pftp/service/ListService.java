package pftp.service;

import pftp.ArgumentParsing;
import pftp.model.Command;
import pftp.model.Param;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ListService {
    public static void list() throws IOException {
        try (Socket clientSocket = ConnectionService.openSocket();
             InputStream in = clientSocket.getInputStream();
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            out.write(Command.LIST.code);
            out.newLine();
            out.write(ArgumentParsing.getParamIntValue(Param.DEPTH));
            out.newLine();
            out.flush();

            byte[] bytes = in.readAllBytes();
            System.out.println(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    public static String getFileTree(File dir, int level, StringBuilder sb, int maxDepth) {
        if (!dir.isDirectory()) return "";

        StringBuilder prefix = new StringBuilder();
        if (level >= 1) {
            prefix.append("│  ".repeat(level - 1));
            prefix.append("├─ ");
        }

        if (level == maxDepth) {
            sb.append(prefix);
            sb.append("...\n");
        } else {
            for (File file : dir.listFiles()) {
                sb.append(prefix);
                sb.append(file.getName());
                if (file.isDirectory()) sb.append("/");
                sb.append("\n");
                if (file.isDirectory()) {
                    getFileTree(file, level + 1, sb, maxDepth);
                }
            }
        }

        return sb.toString();
    }
}
