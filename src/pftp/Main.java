package pftp;

import pftp.model.Param;
import pftp.service.FetchService;
import pftp.service.ListService;
import pftp.service.SendService;

import java.io.IOException;
import java.net.UnknownHostException;


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
                    SendService.send();
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
}
