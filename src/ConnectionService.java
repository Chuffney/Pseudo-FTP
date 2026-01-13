import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectionService {
    public static Socket openSocket() throws IOException {
        InetAddress host = InetAddress.getByName(ArgumentParsing.getParamValue(Param.IP_ADDR));
        return new Socket(host, ArgumentParsing.getPort());
    }
}
