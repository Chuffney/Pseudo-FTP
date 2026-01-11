public enum ResponseCode {
    OK(0),
    BAD_REQUEST(1),
    FORBIDDEN(2);

    final byte code;

    ResponseCode(int code) {
        this.code = (byte) code;
    }
}
