public enum Param {
    FILE_PATH("f", "filepath", null, false),
    PORT("p", "port", "3000", false),
    IP_ADDR("a", "addr", null, false),
    DIR("d", "directory", ".", false);

    final String shortForm;
    final String longForm;
    final String defaultValue;
    final boolean isFlag;

    Param(String shortForm, String longForm, String defaultValue, boolean isFlag) {
        this.shortForm = shortForm;
        this.longForm = longForm;
        this.defaultValue = defaultValue;
        this.isFlag = isFlag;
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
