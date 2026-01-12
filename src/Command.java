public enum Command {
    SEND("send", 's'),
    FETCH("fetch", 'f'),
    LIST("list", 'l');

    final String value;
    final char code;

    Command(String value, char code) {
        this.value = value;
        this.code = code;
    }
}
