import com.fazecast.jSerialComm.SerialPort;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * One instance per COM port.
 * No UI imports. No Swing/AWT knowledge.
 */
public final class DeviceSession {

    // -------------------------
    // Factory
    // -------------------------

    public static DeviceSession create(String comPort) {
        var port = SerialPort.getCommPort(comPort);

        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 200, 0);

        if (!port.openPort()) {
            throw new IllegalStateException("Unable to open port: " + comPort);
        }

        var session = new DeviceSession(port);
        session.start();
        return session;
    }

    // -------------------------
    // Instance state
    // -------------------------

    private final SerialPort port;
    private final Object writeLock = new Object();

    private volatile boolean running;
    private volatile boolean updatesEnabled;

    private Thread readerThread;
    private Thread pollThread;

    private final FrameBuffer frameBuffer = new FrameBuffer();

    private final UpdateCoordinator coordinator;

    private final Map<Field<?>, Consumer<?>> callbacks = new HashMap<>();
    private final Object[] lastValues = new Object[Field.values().length];

    private Runnable onStale;
    private Runnable onFresh;

    private DeviceSession(SerialPort port) {
        this.port = port;
        this.coordinator = new UpdateCoordinator(
                () -> run(onStale),
                () -> run(onFresh)
        );
    }

    // -------------------------
    // Lifecycle
    // -------------------------

    public void stop() {
        running = false;
        if (readerThread != null) readerThread.interrupt();
        if (pollThread != null) pollThread.interrupt();

        synchronized (writeLock) {
            port.closePort();
        }
    }

    private void start() {
        running = true;

        readerThread = new Thread(this::readerLoop, "device-session-reader");
        pollThread = new Thread(this::pollLoop, "device-session-poller");

        readerThread.start();
        pollThread.start();
    }

    // -------------------------
    // Subscription / freshness
    // -------------------------

    public void setUpdatesEnabled(boolean enabled) {
        updatesEnabled = enabled;
        coordinator.setUpdatesEnabled(enabled);
        if (!enabled) coordinator.forceStale();
    }

    public void setOnStale(Runnable r) {
        this.onStale = r;
    }

    public void setOnFresh(Runnable r) {
        this.onFresh = r;
    }

    // -------------------------
    // Callback registration
    // -------------------------

    public <T> void on(Field<T> field, Consumer<T> cb) {
        callbacks.put(field, cb);
    }

    // -------------------------
    // Writes (immediate)
    // -------------------------

    public void setCh1Voltage(BigDecimal v) {
        writeNow(DeviceCommands.setVoltage(1, v));
    }

    public void setCh1Current(BigDecimal a) {
        writeNow(DeviceCommands.setCurrent(1, a));
    }

    public void setCh2Voltage(BigDecimal v) {
        writeNow(DeviceCommands.setVoltage(2, v));
    }

    public void setCh2Current(BigDecimal a) {
        writeNow(DeviceCommands.setCurrent(2, a));
    }

    public void setOutput(boolean on) {
        writeNow(DeviceCommands.setOutput(on));
    }

    public void setSerial(boolean on) {
        writeNow(DeviceCommands.setSerial(on));
    }

    public void setParallel(boolean on) {
        writeNow(DeviceCommands.setParallel(on));
    }

    private void writeNow(byte[] cmd) {
        synchronized (writeLock) {
            port.writeBytes(cmd, cmd.length);
        }
    }

    // -------------------------
    // Poll loop (Read-All)
    // -------------------------

    private void pollLoop() {
        while (running) {
            if (!updatesEnabled) {
                sleepMillis(100);
                continue;
            }

            long now = System.nanoTime();

            if (coordinator.shouldSend(now)) {
                synchronized (writeLock) {
                    byte[] cmd = DeviceCommands.readAll();
                    port.writeBytes(cmd, cmd.length);
                }
                coordinator.onSend(now);
            }

            sleepMillis(100);
        }
    }

    // -------------------------
    // Reader loop (streaming)
    // -------------------------

    private void readerLoop() {
        byte[] chunk = new byte[512];

        while (running) {
            int n = port.readBytes(chunk, chunk.length);
            if (n <= 0) continue;

            frameBuffer.append(chunk, n);

            byte[] frame;
            while ((frame = frameBuffer.tryPopFrame()) != null) {
                handleFrame(frame);
            }
        }
    }

    private void handleFrame(byte[] frame) {
        if (!Frames.isValidFrame(frame)) return;
        if (!Frames.isReadAllResponse(frame)) return;

        coordinator.onReceive(System.nanoTime());
        dispatchFrame(frame);
    }

    // -------------------------
    // Diff + dispatch
    // -------------------------

    private void dispatchFrame(byte[] frame) {
        Field<?>[] fields = Field.values();
        for (int i = 0; i < fields.length; i++) {
            dispatchOne(frame, i, fields[i]);
        }
    }

    private <T> void dispatchOne(byte[] frame, int idx, Field<T> f) {
        T newV = f.read(frame);

        @SuppressWarnings("unchecked")
        T oldV = (T) lastValues[idx];

        if (oldV != null && f.same(oldV, newV)) return;

        lastValues[idx] = newV;
        emit(f, newV);
    }

    @SuppressWarnings("unchecked")
    private <T> void emit(Field<T> f, T value) {
        var cb = (Consumer<T>) callbacks.get(f);
        if (cb != null) cb.accept(value);
    }

    // -------------------------
    // Utils
    // -------------------------

    private static void run(Runnable r) {
        if (r != null) r.run();
    }

    private static void sleepMillis(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/* ============================================================
 * Timing / freshness coordinator
 * ============================================================ */

final class UpdateCoordinator {

    private static final long TICK_NANOS = 100_000_000L;
    private static final long FORCE_RETRY_NANOS = 1_000_000_000L;

    private final Runnable onStale;
    private final Runnable onFresh;

    private boolean updatesEnabled;
    private long lastSendNanos;
    private long lastReceiveNanos;

    UpdateCoordinator(Runnable onStale, Runnable onFresh) {
        this.onStale = onStale;
        this.onFresh = onFresh;
    }

    void setUpdatesEnabled(boolean enabled) {
        updatesEnabled = enabled;
        if (!enabled) onStale.run();
    }

    void forceStale() {
        onStale.run();
    }

    boolean shouldSend(long nowNanos) {
        if (!updatesEnabled) return false;

        boolean receivedRecently = (nowNanos - lastReceiveNanos) < TICK_NANOS;
        boolean forceRetry = (nowNanos - lastSendNanos) >= FORCE_RETRY_NANOS;

        if (!receivedRecently && forceRetry) onStale.run();

        return receivedRecently || forceRetry;
    }

    void onSend(long nowNanos) {
        lastSendNanos = nowNanos;
    }

    void onReceive(long nowNanos) {
        lastReceiveNanos = nowNanos;
        onFresh.run();
    }
}

/* ============================================================
 * Streaming frame buffer (F7 ... FD)
 * ============================================================ */

final class FrameBuffer {

    private byte[] buf = new byte[2048];
    private int len = 0;

    void append(byte[] src, int n) {
        ensure(len + n);
        System.arraycopy(src, 0, buf, len, n);
        len += n;
    }

    byte[] tryPopFrame() {
        int start = indexOf((byte) 0xF7, 0);
        if (start < 0) {
            len = 0;
            return null;
        }

        if (start > 0) shiftLeft(start);

        int end = indexOf((byte) 0xFD, 0);
        if (end < 0) return null;

        int frameLen = end + 1;
        byte[] frame = new byte[frameLen];
        System.arraycopy(buf, 0, frame, 0, frameLen);
        shiftLeft(frameLen);
        return frame;
    }

    private int indexOf(byte b, int from) {
        for (int i = from; i < len; i++) {
            if (buf[i] == b) return i;
        }
        return -1;
    }

    private void shiftLeft(int n) {
        int remaining = len - n;
        if (remaining > 0) System.arraycopy(buf, n, buf, 0, remaining);
        len = remaining;
    }

    private void ensure(int need) {
        if (need <= buf.length) return;
        int cap = buf.length;
        while (cap < need) cap *= 2;
        byte[] nb = new byte[cap];
        System.arraycopy(buf, 0, nb, 0, len);
        buf = nb;
    }
}

/* ============================================================
 * Frame validation and protocol helpers
 * ============================================================ */

final class Frames {

    private Frames() {
    }

    static boolean isValidFrame(byte[] frame) {
        if (frame.length < 6) return false;
        if ((frame[0] & 0xFF) != 0xF7) return false;
        if ((frame[frame.length - 1] & 0xFF) != 0xFD) return false;

        int crcLo = frame[frame.length - 3] & 0xFF;
        int crcHi = frame[frame.length - 2] & 0xFF;
        int got = (crcHi << 8) | crcLo;

        int calc = Crc16Modbus.compute(frame, frame.length - 3);
        return got == calc;
    }

    static boolean isReadAllResponse(byte[] frame) {
        return frame.length == 26
                && (frame[1] & 0xFF) == 0x02
                && (frame[2] & 0xFF) == 0x03
                && (frame[3] & 0xFF) == 0x04
                && (frame[4] & 0xFF) == 0x09;
    }
}

/* ============================================================
 * CRC16 (Modbus)
 * ============================================================ */

final class Crc16Modbus {

    private Crc16Modbus() {
    }

    static int compute(byte[] data, int length) {
        int crc = 0xFFFF;
        for (int i = 0; i < length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                crc = (crc & 1) != 0 ? (crc >> 1) ^ 0xA001 : (crc >> 1);
            }
        }
        return crc & 0xFFFF;
    }
}
