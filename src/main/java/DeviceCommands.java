import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Builds command frames according to the PeakTech 2CH protocol.
 * All commands:
 * F7 ... CRC_LO CRC_HI FD
 */
public final class DeviceCommands {

    private DeviceCommands() {
    }

    // -------------------------------------------------
    // Public commands
    // -------------------------------------------------

    public static byte[] readAll() {
        return withCrcAndEnd(new byte[]{
                (byte) 0xF7,
                (byte) 0x02,
                (byte) 0x03,
                (byte) 0x04,
                (byte) 0x09
        });
    }

    public static byte[] setVoltage(int channel, BigDecimal voltage) {
        return setU16(
                voltage,
                2,
                100,
                (byte) 0x0A,
                channel == 1 ? (byte) 0x0B : (byte) 0x09
        );
    }

    public static byte[] setCurrent(int channel, BigDecimal current) {
        return setU16(
                current,
                3,
                1000,
                (byte) 0x0A,
                channel == 1 ? (byte) 0x0D : (byte) 0x0C
        );
    }

    public static byte[] setOutput(boolean on) {
        return setFlag((byte) 0x1E, on ? 1 : 0);
    }

    public static byte[] setSerial(boolean on) {
        return setFlag((byte) 0x1F, on ? 1 : 0);
    }

    public static byte[] setParallel(boolean on) {
        return setFlag((byte) 0x1F, on ? 2 : 0);
    }

    // -------------------------------------------------
    // Internals
    // -------------------------------------------------

    private static byte[] setFlag(byte address, int value) {
        return withCrcAndEnd(new byte[]{
                (byte) 0xF7,
                (byte) 0x02,
                (byte) 0x0A,
                address,
                (byte) 0x01,
                (byte) 0x00,
                (byte) value
        });
    }

    private static byte[] setU16(
            BigDecimal value,
            int scale,
            int factor,
            byte function,
            byte address
    ) {
        BigDecimal scaled = value.setScale(scale, RoundingMode.UNNECESSARY);
        int raw = scaled.multiply(BigDecimal.valueOf(factor)).intValueExact();

        byte hi = (byte) ((raw >> 8) & 0xFF);
        byte lo = (byte) (raw & 0xFF);

        return withCrcAndEnd(new byte[]{
                (byte) 0xF7,
                (byte) 0x02,
                function,
                address,
                (byte) 0x01,
                hi,
                lo
        });
    }

    private static byte[] withCrcAndEnd(byte[] payload) {
        int crc = Crc16Modbus.compute(payload, payload.length);

        byte crcLo = (byte) (crc & 0xFF);
        byte crcHi = (byte) ((crc >> 8) & 0xFF);

        byte[] out = new byte[payload.length + 3];
        System.arraycopy(payload, 0, out, 0, payload.length);
        out[payload.length] = crcLo;
        out[payload.length + 1] = crcHi;
        out[payload.length + 2] = (byte) 0xFD;

        return out;
    }
}
