import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    private final List<SerialPortInfo> ports;

    public MainWindow(List<SerialPortInfo> ports) {
        this.ports = ports;

        setTitle("Lab Device Controller");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(Color.BLACK);
        getContentPane().setLayout(new BorderLayout());

        showDeviceSelection();

        setVisible(true);
    }

    private void showDeviceSelection() {
        setContent(DeviceSelectionView.createView(
                ports,
                this::showDeviceOverview
        ));
    }

    private void showDeviceOverview(SerialPortInfo device) {
        setContent(DeviceOverviewView.createView(
                device,
                this::showDeviceSelection
        ));
    }

    private void setContent(JComponent component) {
        var content = getContentPane();
        content.removeAll();
        content.add(component, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}
