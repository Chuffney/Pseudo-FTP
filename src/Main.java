import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;


public class Main {
    private static final int MAX_DEPTH = 5;

    private static final String HELP_MESSAGE = """
            Usage:
                pftp [COMMAND] [OPTIONS]
            
            Description:
                A simple server/client program for sending and receiving files.
            
            Commands:
                send    - start the server which listens for incoming requests.
                fetch   - fetches the file with the name given in the "-f" option.
                list    - displays available files.
            
            Options:
                -h, --help
                    Shows this help message.
            
                -f, --file
                    Specifies the file to be fetched.
            
                -p, --port
                    Specifies the port on which the server listens / client requests.
            
                -a, --addr
                    Specifies the address to which the requests are sent.
            
                -d, --directory
                    Specifies the working directory of the server.
            """;

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("--help")) {
            System.out.println(HELP_MESSAGE);
            return;
        }

        ArgumentParsing.parseArgs(args);

        if (ArgumentParsing.getCommand() == null) {
            String availableCommands = Arrays.stream(Command.values()).map(c -> c.value).collect(Collectors.joining(", "));
            System.err.println("Available commands: " + availableCommands);
            return;
        }

        try {
            switch (ArgumentParsing.getCommand()) {
                case FETCH:
                    FetchService.fetch();
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
        ServerSocket serverSocket = new ServerSocket(ArgumentParsing.getPort());
        System.out.println("awaiting requests");

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

            if (Command.LIST.code == operation) {
                System.out.println("LIST");
                StringBuilder sb = new StringBuilder();
                String fileTree = getFileTree(workingDir, 0, sb);
                out.write(fileTree.getBytes());
            } else if (Command.FETCH.code == operation) {
                System.out.println("FETCH: " + reqBody);

                File file = new File(workingDir, reqBody);
                if (!file.exists()) {
                    out.write(ResponseCode.NOT_FOUND.code);
                } else if (!file.getCanonicalPath().startsWith(workingDir.getCanonicalPath())) {
                    out.write(ResponseCode.FORBIDDEN.code);
                } else {
                    out.write(ResponseCode.OK.code);
                    out.write(longToBytes(file.length()));
                    FileInputStream fis = new FileInputStream(file);
                    fis.transferTo(out);
                    fis.close();
                }
            } else {
                System.out.println("UNKNOWN: " + operation);
                out.write(ResponseCode.BAD_REQUEST.code);
            }
            clientSocket.close();
        }
    }

    private static void list() throws IOException {
        try (Socket clientSocket = ConnectionService.openSocket();
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
            prefix.append("│  ".repeat(level - 1));
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

    public static byte[] longToBytes(long l) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l);
        return buffer.array();
    }

    public static void terminateEarly() {
        System.exit(-1);
    }
}
