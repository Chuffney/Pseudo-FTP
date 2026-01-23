package pftp.model;

public enum Param {
    FILE_PATH("f", "filepath", null, false),
    PORT("p", "port", "3000", true),
    IP_ADDR("a", "addr", null, false),
    DIR("d", "directory", ".", false);

    public final String shortForm;
    public final String longForm;
    public final String defaultValue;
    public final boolean isNumber;
    public final boolean isFlag;

    Param(String shortForm, String longForm, String defaultValue, boolean isNumber) {
        this.shortForm = shortForm;
        this.longForm = longForm;
        this.defaultValue = defaultValue;
        this.isNumber = isNumber;
        this.isFlag = false;
    }

    Param(String shortForm, String longForm) {
        this.shortForm = shortForm;
        this.longForm = longForm;
        this.defaultValue = null;
        this.isNumber = false;
        this.isFlag = true;
    }

    public static Param getByLongForm(String longForm) {
        for (Param param : values()) {
            if (param.longForm.equalsIgnoreCase(longForm))
                return param;
        }
        return null;
    }

    public static Param getByShortForm(String shortForm) {
        for (Param param : values()) {
            if (param.shortForm.equalsIgnoreCase(shortForm))
                return param;
        }
        return null;
    }
}
