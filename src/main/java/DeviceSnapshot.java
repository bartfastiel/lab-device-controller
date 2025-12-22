import java.math.BigDecimal;

public record DeviceSnapshot(
        Channel ch1,
        Channel ch2
) {
    public record Channel(
            BigDecimal voltageMeasured,
            BigDecimal currentMeasured,
            BigDecimal voltageSet,
            BigDecimal currentSet,
            boolean cv,
            boolean cc,
            boolean output,
            boolean serial,
            boolean parallel
    ) {}
}
