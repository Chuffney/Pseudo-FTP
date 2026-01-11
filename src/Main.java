import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class Main {
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("--help")) {
            System.out.println("Usage: pftp --send\nor: pftp: --fetch IPaddr fileAddr");
        }

        ArgumentParsing.parseArgs(args);

        boolean receiving = ArgumentParsing.paramMap.get(Param.FETCH).equals("true");
        boolean sending = ArgumentParsing.paramMap.get(Param.SEND).equals("true");
        int port = Integer.parseInt(ArgumentParsing.paramMap.get(Param.PORT));

        try {
            if (receiving) {
                InetAddress host = InetAddress.getByName(ArgumentParsing.paramMap.get(Param.IP_ADDR));
                Socket clientSocket = new Socket(host, port);

                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out);
                BufferedWriter bw = new BufferedWriter(osw);

                String fileName = ArgumentParsing.paramMap.get(Param.FILE_PATH);
                bw.write(fileName);
                bw.newLine();
                bw.flush();

                System.out.println("sent request");

                FileOutputStream fos = new FileOutputStream(fileName);
                in.transferTo(fos);
                fos.close();

                clientSocket.close();
            } else if (sending) {
                ServerSocket serverSocket = new ServerSocket(port);
                Socket clientSocket = serverSocket.accept();

                System.out.println("socket opened");

                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();
                InputStreamReader isr = new InputStreamReader(in);
                 BufferedReader br = new BufferedReader(isr);

                String filePath = br.readLine();

                System.out.println("file requested: " + filePath);

                FileInputStream fis = new FileInputStream(filePath);
                fis.transferTo(out);
                fis.close();

                clientSocket.close();
                serverSocket.close();
            } else {
                System.err.println("specify either --send or --fetch flag");
            }
        } catch (UnknownHostException ignored) {
            System.err.println("unknown host");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void terminateEarly() {
        System.exit(-1);
    }
}
