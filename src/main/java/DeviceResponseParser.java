import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DeviceResponseParser {

    private static final BigDecimal VOLT_SCALE = new BigDecimal("0.01");
    private static final BigDecimal AMP_SCALE  = new BigDecimal("0.001");

    private DeviceResponseParser() {}

    public static DeviceSnapshot parse(byte[] data, int len) {
        if (len < 26)
            throw new IllegalArgumentException("Response too short: " + len);

        if ((data[0] & 0xFF) != 0xF7)
            throw new IllegalArgumentException("Invalid start byte");

        if ((data[25] & 0xFF) != 0xFD)
            throw new IllegalArgumentException("Invalid end byte");

        int ch2Status = data[5] & 0xFF;
        int ch1Status = data[6] & 0xFF;

        var ch1 = new DeviceSnapshot.Channel(
                volts(data, 11),
                amps(data, 13),
                volts(data, 19),
                amps(data, 21),
                bit(ch1Status, 0),
                bit(ch1Status, 1),
                bit(ch2Status, 5),
                bit(ch2Status, 2),
                bit(ch2Status, 3)
        );

        var ch2 = new DeviceSnapshot.Channel(
                volts(data, 7),
                amps(data, 9),
                volts(data, 15),
                amps(data, 17),
                bit(ch2Status, 0),
                bit(ch2Status, 1),
                bit(ch2Status, 5),
                bit(ch2Status, 2),
                bit(ch2Status, 3)
        );

        return new DeviceSnapshot(ch1, ch2);
    }

    private static boolean bit(int value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    private static BigDecimal volts(byte[] d, int i) {
        int raw = ((d[i] & 0xFF) << 8) | (d[i + 1] & 0xFF);
        return new BigDecimal(raw).multiply(VOLT_SCALE).setScale(2, RoundingMode.UNNECESSARY);
    }

    private static BigDecimal amps(byte[] d, int i) {
        int raw = ((d[i] & 0xFF) << 8) | (d[i + 1] & 0xFF);
        return new BigDecimal(raw).multiply(AMP_SCALE).setScale(3, RoundingMode.UNNECESSARY);
    }
}
