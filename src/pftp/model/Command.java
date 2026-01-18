package pftp.model;

public enum Command {
    SEND("send", 's'),
    FETCH("fetch", 'f'),
    LIST("list", 'l');

    public final String value;
    public final char code;

    Command(String value, char code) {
        this.value = value;
        this.code = code;
    }

    public static Command getByValue(String value) {
        for (Command command : values()) {
            if (command.value.equalsIgnoreCase(value))
                return command;
        }
        return null;
    }
}
