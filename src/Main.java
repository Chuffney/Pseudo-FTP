import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;


public class Main {
    private static final int MAX_DEPTH = 5;

    private static final File workingDir = new File(".");

    static void main(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("--help")) {
            System.out.println("Usage: pftp --send\nor: pftp: --fetch IPaddr fileAddr");
        }

        ArgumentParsing.parseArgs(args);

        if (ArgumentParsing.getCommand() == null) {
            String availableCommands = Arrays.stream(Command.values()).map(c -> c.value).collect(Collectors.joining(", "));
            System.err.println("Available commands: " + availableCommands);
            return;
        }

        try {
            switch (ArgumentParsing.getCommand()) {
                case Command.FETCH:
                    fetch();
                    break;
                case SEND:
                    send();
                    break;
                case LIST:
                    list();
                    break;
            }
        } catch (UnknownHostException ignored) {
            System.err.println("unknown host");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void send() throws IOException {
        System.out.println("socket opened");

        while (true) {
            ServerSocket serverSocket = new ServerSocket(ArgumentParsing.getPort());
            Socket clientSocket = serverSocket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();


            String request = in.readLine();
            char operation = request.charAt(0);
            String body = request.substring(1);

            if (Command.LIST.code == operation) {
                StringBuilder sb = new StringBuilder();
                String fileTree = getFileTree(workingDir, 0, sb);
                out.write(fileTree.getBytes());
            } else if (Command.FETCH.code == operation) {
                System.out.println("file requested: " + body);

                File file = new File(body);
                if (!file.exists()) {
                    out.write(ResponseCode.BAD_REQUEST.code);
                } else if (!file.getCanonicalPath().startsWith(workingDir.getCanonicalPath())) {
                    out.write(ResponseCode.FORBIDDEN.code);
                } else {
                    out.write(ResponseCode.OK.code);
                    FileInputStream fis = new FileInputStream(body);
                    fis.transferTo(out);
                    fis.close();
                }
            }

            clientSocket.close();
            serverSocket.close();
        }
    }

    private static void fetch() throws IOException {
        try (Socket clientSocket = openSocket();
             InputStream in = clientSocket.getInputStream();
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            String filePath = ArgumentParsing.paramMap.get(Param.FILE_PATH);
            out.write(Command.FETCH.code);
            out.write(filePath);
            out.newLine();
            out.flush();

            System.out.println("sent request");

            byte code = (byte) in.read();

            if (code == ResponseCode.BAD_REQUEST.code) {
                System.out.println("File not found!");
            } else if (code == ResponseCode.FORBIDDEN.code) {
                System.out.println("Access denied!");
            } else if (code == ResponseCode.OK.code) {
                String fileName = new File(filePath).getName();
                System.out.println("Transfering file to " + fileName);
                FileOutputStream fos = new FileOutputStream(fileName);
                in.transferTo(fos);
                fos.close();
            }
        }
    }

    private static void list() throws IOException {
        try (Socket clientSocket = openSocket();
             InputStream in = clientSocket.getInputStream();
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            out.write(Command.LIST.code);
            out.newLine();
            out.flush();

            byte[] bytes = in.readAllBytes();
            System.out.println(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    private static String getFileTree(File dir, int level, StringBuilder sb) {
        if (!dir.isDirectory()) return "";

        StringBuilder prefix = new StringBuilder();
        if (level >= 1) {
            for (int i = 1; i < level; i++) {
                prefix.append("│  ");
            }
            prefix.append("├─ ");
        }

        if (level == MAX_DEPTH) {
            sb.append(prefix);
            sb.append("...\n");
        }

        for (File file : dir.listFiles()) {
            sb.append(prefix);
            sb.append(file.getName());
            sb.append("\n");
            if (file.isDirectory()) {
                getFileTree(file, level + 1, sb);
            }
        }

        return sb.toString();
    }

    private static Socket openSocket() throws IOException {
        InetAddress host = InetAddress.getByName(ArgumentParsing.paramMap.get(Param.IP_ADDR));
        return new Socket(host, ArgumentParsing.getPort());
    }

    public static void terminateEarly() {
        System.exit(-1);
    }
}
