import java.math.BigDecimal;
import java.util.function.Consumer;

public record DeviceActions(

        Consumer<BigDecimal> setCh1Voltage,
        Consumer<BigDecimal> setCh1Current,

        Consumer<BigDecimal> setCh2Voltage,
        Consumer<BigDecimal> setCh2Current,

        Consumer<Boolean> setOutput,
        Consumer<Boolean> setSerial,
        Consumer<Boolean> setParallel
) {
}
