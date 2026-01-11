public enum Param {
    SEND("", "send"),
    FETCH("", "fetch"),
    FILE_PATH("f", "filepath", null, false),
    PORT("p", "port", "3000", false),
    IP_ADDR("a", "addr", null, false);

    final String shortForm;
    final String longForm;
    String defaultValue = "";
    boolean isFlag = true;

    Param(String shortForm, String longForm) {
        this.shortForm = shortForm;
        this.longForm = longForm;
    }

    Param(String shortForm, String longForm, String defaultValue, boolean isFlag) {
        this.shortForm = shortForm;
        this.longForm = longForm;
        this.defaultValue = defaultValue;
        this.isFlag = isFlag;
    }
}
