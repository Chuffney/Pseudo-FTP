package pftp;

import pftp.model.Command;
import pftp.model.Param;
import pftp.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class ArgumentParsing {
    private static final Map<Param, String> paramMap = new HashMap<>();
    private static Command command = null;

    public static boolean parseArgs(String[] argStr) {
        Iterator<String> iterator = Arrays.stream(argStr).iterator();

        String commandToken = iterator.next();
        command = Command.getByValue(commandToken);

        if (command == null) {
            String availableCommands = Arrays.stream(Command.values()).map(c -> c.value).collect(Collectors.joining(", "));
            System.err.println("Available commands: " + availableCommands);
            return false;
        }

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
                return false;
            }

            if (arg == null) {
                System.err.println("Unknown parameter: " + paramToken);
                return false;
            }

            if (arg.isFlag) {
                paramMap.put(arg, "true");
                continue;
            }

            if (!iterator.hasNext()) {
                System.out.println("No value provided for parameter " + paramToken);
                return false;
            }
            String argToken = iterator.next();

            if (arg.isNumber && !StringUtil.isNumber(argToken)) {
                System.err.println("The value of the " + paramToken + " parameter must be numeric.");
                return false;
            }
            paramMap.put(arg, argToken);
        }
        return true;
    }

    public static Command getCommand() {
        return command;
    }

    public static String getParamValue(Param param) {
        return paramMap.get(param);
    }

    public static int getParamIntValue(Param param) {
        if (!param.isNumber)
            throw new IllegalArgumentException("Parameter " + param + " is not a number!");

        return Integer.parseInt(paramMap.get(param));
    }
}
