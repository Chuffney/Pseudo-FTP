public enum ResponseCode {
    OK(0),
    NOT_FOUND(1),
    FORBIDDEN(2);

    final byte code;

    ResponseCode(int code) {
        this.code = (byte) code;
    }
}
