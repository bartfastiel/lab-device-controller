import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

public final class DeviceSelectionView {

    private DeviceSelectionView() {}

    public static JComponent createView(
            List<SerialPortInfo> ports,
            Consumer<SerialPortInfo> onSelect
    ) {
        var root = LabPanel.vertical(40);

        if (ports.isEmpty()) {
            root.add(LabLabel.create(
                    "Please plug in a serial device and restart the application."
            ));
            return root;
        }

        root.add(LabLabel.create("Select the device:"));
        root.add(LabSpacer.vertical(25));

        for (var port : ports) {
            var button = LabButton.create(port.toString());
            button.addActionListener(_ -> onSelect.accept(port));

            root.add(button);
            root.add(LabSpacer.vertical(15));
        }

        return root;
    }
}
