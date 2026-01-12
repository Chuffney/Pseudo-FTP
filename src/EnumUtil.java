import java.util.function.Predicate;

public class EnumUtil {
    public static<T extends Enum<?>> T findEnumValue(T[] values, Predicate<T> predicate) {
        for (T t : values) {
            if (predicate.test(t))
                return t;
        }
        return null;
    }
}