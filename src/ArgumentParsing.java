import java.util.*;

public class ArgumentParsing {
    private static final Map<Param, String> paramMap = new HashMap<>();
    private static Command command = null;

    public static void parseArgs(String[] argStr) {
        Iterator<String> iterator = Arrays.stream(argStr).iterator();

        if (!iterator.hasNext()) {
            return;
        }

        String commandToken = iterator.next();
        command = Command.getByValue(commandToken);

        for (Param param : Param.values()) {
            if (param.defaultValue != null)
                paramMap.put(param, param.defaultValue);
        }

        while (iterator.hasNext()) {
            String paramToken = iterator.next();
            Param arg;

            if (paramToken.startsWith("--") && paramToken.length() > 2) {
                String tokenBody = paramToken.substring(2);
                arg = Param.getByLongForm(tokenBody);
            } else if (paramToken.charAt(0) == '-' && paramToken.length() > 1) {
                String tokenBody = paramToken.substring(1);
                arg = Param.getByShortForm(tokenBody);
            } else {
                System.err.println("Unexpected argument: " + paramToken);
                return;
            }

            if (arg == null) {
                System.err.println("Unknown parameter: " + paramToken);
                return;
            }

            if (arg.isFlag) {
                paramMap.put(arg, "true");
                continue;
            }

            String argToken = iterator.next();
            paramMap.put(arg, argToken);
        }
    }

    public static int getPort() {
        return Integer.parseInt(paramMap.get(Param.PORT));
    }

    public static Command getCommand() {
        return command;
    }

    public static String getParamValue(Param param) {
        return paramMap.get(param);
    }
}
