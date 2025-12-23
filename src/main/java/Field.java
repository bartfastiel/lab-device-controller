import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class Field<T> {

    // =========================
    // Public "enum-like" fields
    // =========================

    public static final Field<BigDecimal> CH1_V_MEAS =
            new Field<>(
                    "CH1_V_MEAS",
                    f -> u16Scaled(f, 11, 12, 2),
                    BigDecimal::equals
            );

    public static final Field<BigDecimal> CH1_I_MEAS =
            new Field<>(
                    "CH1_I_MEAS",
                    f -> u16Scaled(f, 13, 14, 3),
                    BigDecimal::equals
            );

    public static final Field<BigDecimal> CH1_V_SET =
            new Field<>(
                    "CH1_V_SET",
                    f -> u16Scaled(f, 19, 20, 2),
                    BigDecimal::equals
            );

    public static final Field<BigDecimal> CH1_I_SET =
            new Field<>(
                    "CH1_I_SET",
                    f -> u16Scaled(f, 21, 22, 3),
                    BigDecimal::equals
            );

    public static final Field<Boolean> CH1_CV =
            new Field<>(
                    "CH1_CV",
                    f -> bit(f[6], 0),
                    (a, b) -> a == b
            );

    public static final Field<Boolean> CH1_CC =
            new Field<>(
                    "CH1_CC",
                    f -> bit(f[6], 1),
                    (a, b) -> a == b
            );

    public static final Field<BigDecimal> CH2_V_MEAS =
            new Field<>(
                    "CH2_V_MEAS",
                    f -> u16Scaled(f, 7, 8, 2),
                    BigDecimal::equals
            );

    public static final Field<BigDecimal> CH2_I_MEAS =
            new Field<>(
                    "CH2_I_MEAS",
                    f -> u16Scaled(f, 9, 10, 3),
                    BigDecimal::equals
            );

    public static final Field<BigDecimal> CH2_V_SET =
            new Field<>(
                    "CH2_V_SET",
                    f -> u16Scaled(f, 15, 16, 2),
                    BigDecimal::equals
            );

    public static final Field<BigDecimal> CH2_I_SET =
            new Field<>(
                    "CH2_I_SET",
                    f -> u16Scaled(f, 17, 18, 3),
                    BigDecimal::equals
            );

    public static final Field<Boolean> CH2_CV =
            new Field<>(
                    "CH2_CV",
                    f -> bit(f[5], 0),
                    (a, b) -> a == b
            );

    public static final Field<Boolean> CH2_CC =
            new Field<>(
                    "CH2_CC",
                    f -> bit(f[5], 1),
                    (a, b) -> a == b
            );

    public static final Field<Boolean> OUTPUT =
            new Field<>(
                    "OUTPUT",
                    f -> bit(f[5], 5),
                    (a, b) -> a == b
            );

    public static final Field<Boolean> SERIAL =
            new Field<>(
                    "SERIAL",
                    f -> bit(f[5], 2),
                    (a, b) -> a == b
            );

    public static final Field<Boolean> PARALLEL =
            new Field<>(
                    "PARALLEL",
                    f -> bit(f[5], 3),
                    (a, b) -> a == b
            );

    // =========================
    // "values()" equivalent
    // =========================

    private static final Field<?>[] ALL = {
            CH1_V_MEAS,
            CH1_I_MEAS,
            CH1_V_SET,
            CH1_I_SET,
            CH1_CV,
            CH1_CC,
            CH2_V_MEAS,
            CH2_I_MEAS,
            CH2_V_SET,
            CH2_I_SET,
            CH2_CV,
            CH2_CC,
            OUTPUT,
            SERIAL,
            PARALLEL
    };

    public static Field<?>[] values() {
        return ALL.clone();
    }

    // =========================
    // Instance part
    // =========================

    private final String name;
    private final Function<byte[], T> reader;
    private final BiPredicate<T, T> equality;

    private Field(
            String name,
            Function<byte[], T> reader,
            BiPredicate<T, T> equality
    ) {
        this.name = name;
        this.reader = reader;
        this.equality = equality;
    }

    public String name() {
        return name;
    }

    T read(byte[] frame) {
        return reader.apply(frame);
    }

    boolean same(T a, T b) {
        return equality.test(a, b);
    }

    // =========================
    // Helpers (local, no dead code)
    // =========================

    private static boolean bit(byte b, int idx) {
        return ((b >> idx) & 1) == 1;
    }

    private static BigDecimal u16Scaled(byte[] f, int hi, int lo, int scale) {
        int raw = ((f[hi] & 0xFF) << 8) | (f[lo] & 0xFF);
        return BigDecimal
                .valueOf(raw)
                .movePointLeft(scale)
                .setScale(scale, RoundingMode.UNNECESSARY);
    }

    @Override
    public String toString() {
        return name;
    }
}
