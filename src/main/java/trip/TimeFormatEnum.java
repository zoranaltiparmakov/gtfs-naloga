package trip;

public enum TimeFormatEnum {
    RELATIVE, ABSOLUTE;

    public static TimeFormatEnum value(String value) {
        return TimeFormatEnum.valueOf(value.toUpperCase());
    }
}
