import javax.swing.SwingUtilities;
import java.util.function.Consumer;

public final class FieldBinder {

    private FieldBinder() {}

    public static void bind(
            DeviceSession session,
            OverviewBindings bindings
    ) {

        bind(session, Field.CH1_V_MEAS, bindings.ch1VoltageMeasured());
        bind(session, Field.CH1_I_MEAS, bindings.ch1CurrentMeasured());
        bind(session, Field.CH1_V_SET,  bindings.ch1VoltageSet());
        bind(session, Field.CH1_I_SET,  bindings.ch1CurrentSet());
        bind(session, Field.CH1_CV,     bindings.ch1CV());
        bind(session, Field.CH1_CC,     bindings.ch1CC());

        bind(session, Field.CH2_V_MEAS, bindings.ch2VoltageMeasured());
        bind(session, Field.CH2_I_MEAS, bindings.ch2CurrentMeasured());
        bind(session, Field.CH2_V_SET,  bindings.ch2VoltageSet());
        bind(session, Field.CH2_I_SET,  bindings.ch2CurrentSet());
        bind(session, Field.CH2_CV,     bindings.ch2CV());
        bind(session, Field.CH2_CC,     bindings.ch2CC());

        bind(session, Field.OUTPUT,     bindings.output());
        bind(session, Field.SERIAL,     bindings.serial());
        bind(session, Field.PARALLEL,   bindings.parallel());
    }

    private static <T> void bind(
            DeviceSession session,
            Field<T> field,
            Consumer<T> target
    ) {
        session.on(field, value ->
                SwingUtilities.invokeLater(() -> target.accept(value))
        );
    }
}
