import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class MainWindow {

    private MainWindow() {}

    public static void show(List<SerialPortInfo> ports) {
        var frame = new JFrame("Lab Device Controller");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(965, 440);
        frame.setLocationRelativeTo(null);

        var host = new JPanel(new CardLayout());
        host.setBackground(Color.BLACK);

        var state = new State(host, ports);

        frame.setContentPane(host);
        frame.setVisible(true);

        state.showSelection();
    }

    // -------------------------------------------------
    // Screens
    // -------------------------------------------------

    private enum Screen {
        SELECTION("selection"),
        OVERVIEW("overview");

        final String key;

        Screen(String key) {
            this.key = key;
        }
    }

    // -------------------------------------------------
    // State / Glue
    // -------------------------------------------------

    private static final class State {

        private final JPanel host;
        private final List<SerialPortInfo> ports;

        private DeviceSession session;

        State(JPanel host, List<SerialPortInfo> ports) {
            this.host = host;
            this.ports = ports;
        }

        // -----------------------------
        // Navigation
        // -----------------------------

        void showSelection() {
            closeSessionIfAny();

            var selectionView = DeviceSelectionView.createView(
                    ports,
                    this::openOverview
            );

            host.add(selectionView, Screen.SELECTION.key);
            show(Screen.SELECTION);
        }

        void openOverview(SerialPortInfo selected) {
            closeSessionIfAny();

            session = DeviceSession.create(selected.systemPortName());

            var hb = new java.util.concurrent.atomic.AtomicBoolean(false);

            var actions = new DeviceActions(
                    session::setCh1Voltage,
                    session::setCh1Current,
                    session::setCh2Voltage,
                    session::setCh2Current,
                    session::setOutput,
                    session::setSerial,
                    session::setParallel
            );

            var overview = DeviceOverviewView.createView(
                    selected,
                    actions,
                    this::showSelection
            );

            host.add(overview.view(), Screen.OVERVIEW.key);

            // Device -> UI bindings
            FieldBinder.bind(session, overview.bindings());

            session.setOnStale(() -> {
                SwingUtilities.invokeLater(overview.bindings().setStale());
                overview.bindings().heartbeat().accept(false);
            });

            session.setOnFresh(() -> SwingUtilities.invokeLater(() -> {
                overview.bindings().setFresh().run();

                boolean next;
                boolean prev;

                prev = hb.get();
                next = !prev;
                hb.set(next);

                overview.bindings().heartbeat().accept(next);
            }));

            session.setUpdatesEnabled(true);
            show(Screen.OVERVIEW);
        }

        // -----------------------------
        // Helpers
        // -----------------------------

        private void show(Screen screen) {
            var cl = (CardLayout) host.getLayout();
            cl.show(host, screen.key);
            host.revalidate();
            host.repaint();
        }

        private void closeSessionIfAny() {
            if (session == null) {
                return;
            }

            session.setUpdatesEnabled(false);
            session.stop();
            session = null;
        }
    }
}
