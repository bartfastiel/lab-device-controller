import javax.swing.*;
import java.awt.*;

public final class DeviceOverviewView {

    private DeviceOverviewView() {}

    public static JComponent createView(
            SerialPortInfo device,
            Runnable onBack
    ) {
        var root = LabPanel.border(30);

        var backButton = LabButton.create(device.toString());
        backButton.addActionListener(_ -> onBack.run());

        root.add(backButton, BorderLayout.NORTH);

        return root;
    }
}
