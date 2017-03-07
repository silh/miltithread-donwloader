package org.http.donwloader.multithread.input;

public enum InputParamType {
    NUMBER_OF_THREADS("-n"),
    SPEED("-l"),
    PATH_TO_FILE("-f"),
    PATH_TO_RESULT("-o");

    private final String param;

    InputParamType(String param) {
        this.param = param;
    }

    public static InputParamType getParam(String paramName) {
        InputParamType[] values = values();
        for (InputParamType value : values) {
            if (value.param.equals(paramName)) {
                return value;
            }
        }
        return null;
    }
}
