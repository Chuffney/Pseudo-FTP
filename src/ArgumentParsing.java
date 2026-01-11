import java.util.*;

public class ArgumentParsing {
    public static final Map<Param, String> paramMap = new HashMap<>();

    public static void parseArgs(String[] argStr) {
        Set<Param> remainingParams = new HashSet<>(List.of(Param.values()));
//        StringTokenizer tokenizer = new StringTokenizer(argStr);
        Iterator<String> tokenizer = Arrays.stream(argStr).iterator();

        while (tokenizer.hasNext()) {
            String paramToken = tokenizer.next();
            Param arg = null;

            if (paramToken.startsWith("--") && paramToken.length() > 2) {
                String tokenBody = paramToken.substring(2);

                for (Param param : Param.values()) {
                    if (param.longForm.equalsIgnoreCase(tokenBody)) {
                        if (!remainingParams.contains(param)) {
                            System.err.println("Duplicated parameter: " + paramToken);
                            return;
                        }

                        arg = param;
                        remainingParams.remove(param);
                        break;
                    }
                }
            } else if (paramToken.charAt(0) == '-' && paramToken.length() > 1) {
                String tokenBody = paramToken.substring(1);

                for (Param param : Param.values()) {
                    if (param.shortForm.equalsIgnoreCase(tokenBody)) {
                        if (!remainingParams.contains(param)) {
                            System.err.println("Duplicated parameter: " + paramToken);
                            return;
                        }

                        arg = param;
                        remainingParams.remove(param);
                    }
                }
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

            String argToken = tokenizer.next();
            paramMap.put(arg, argToken);

        }
        for (Param param : remainingParams) {
            if (param.defaultValue != null)
                paramMap.put(param, param.defaultValue);
        }
    }
}
