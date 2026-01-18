package pftp;

import pftp.model.Command;
import pftp.model.Param;
import pftp.model.ResponseCode;
import pftp.service.FetchService;
import pftp.service.ListService;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Main {
    private static final String HELP_MESSAGE = """
            Usage:
                pftp [COMMAND] [OPTIONS]
            
            Description:
                A simple server/client for sending and receiving files.
            
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

        String errorMessage = ArgumentParsing.parseArgs(args);

        if (errorMessage != null) {
            System.err.println(errorMessage);
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
                    ListService.list();
                    break;
            }
        } catch (UnknownHostException ignored) {
            System.err.println("unknown host (" + ArgumentParsing.getParamValue(Param.IP_ADDR) + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void send() throws IOException {
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

            if (Command.LIST.code == operation) {
                System.out.println("LIST" + reqBody);
                String fileList = ListService.listFiles(reqBody, file);
                out.write(fileList.getBytes());
            }
            else if (Command.FETCH.code == operation) {
                System.out.println("FETCH: " + reqBody);

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
            }
            else {
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

    public static void terminateEarly() {
        System.exit(-1);
    }
}
