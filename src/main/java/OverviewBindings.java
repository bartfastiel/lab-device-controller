import java.math.BigDecimal;
import java.util.function.Consumer;

public record OverviewBindings(

        Runnable setStale,
        Runnable setFresh,
        Consumer<Boolean> heartbeat,

        Consumer<BigDecimal> ch1VoltageMeasured,
        Consumer<BigDecimal> ch1CurrentMeasured,
        Consumer<BigDecimal> ch1VoltageSet,
        Consumer<BigDecimal> ch1CurrentSet,
        Consumer<Boolean>   ch1CV,
        Consumer<Boolean>   ch1CC,

        Consumer<BigDecimal> ch2VoltageMeasured,
        Consumer<BigDecimal> ch2CurrentMeasured,
        Consumer<BigDecimal> ch2VoltageSet,
        Consumer<BigDecimal> ch2CurrentSet,
        Consumer<Boolean>   ch2CV,
        Consumer<Boolean>   ch2CC,

        Consumer<Boolean> output,
        Consumer<Boolean> serial,
        Consumer<Boolean> parallel
) {
}
