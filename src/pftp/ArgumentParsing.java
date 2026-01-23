package pftp;

import pftp.model.Command;
import pftp.model.Param;
import pftp.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ArgumentParsing {
    private static final Map<Param, String> paramMap = new HashMap<>();
    private static Command command = null;

    public static String parseArgs(String[] argStr) {
        Iterator<String> iterator = Arrays.stream(argStr).iterator();

        if (!iterator.hasNext())
            return "";

        String commandToken = iterator.next();
        command = Command.getByValue(commandToken);

        if (command == null) {
            String availableCommands = Arrays.stream(Command.values()).map(c -> c.value).collect(Collectors.joining(", "));
            return "Available commands: " + availableCommands;
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
                return "Unexpected argument: " + paramToken;
            }

            if (arg == null) {
                return "Unknown parameter: " + paramToken;
            }

            if (arg.isFlag) {
                paramMap.put(arg, "true");
                continue;
            }

            if (!iterator.hasNext()) {
                return "No value provided for parameter " + paramToken;
            }
            String argToken = iterator.next();

            if (arg.isNumber && !StringUtil.isNumber(argToken)) {
                return "The value of the " + paramToken + " parameter must be numeric.";
            }

            paramMap.put(arg, argToken);
        }

        return null;
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
