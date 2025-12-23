import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public final class DeviceSelectionView {

    private DeviceSelectionView() {}

    public static JComponent createView(
            List<SerialPortInfo> ports,
            Consumer<SerialPortInfo> onSelect
    ) {
        var root = LabPanel.border(40);

        var content = new JPanel();
        content.setBackground(Color.BLACK);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(LabLabel.create("Select a device"));
        content.add(Box.createVerticalStrut(30));

        if (ports.isEmpty()) {
            content.add(LabLabel.create(
                    "No supported device found.\nPlease connect a device and restart."
            ));
        } else {
            for (var port : ports) {
                var button = LabButton.create(port.toString());
                button.addActionListener(_ -> onSelect.accept(port));

                content.add(button);
                content.add(Box.createVerticalStrut(15));
            }
        }

        root.add(content, BorderLayout.CENTER);
        return root;
    }
}
