import javax.swing.*;
import java.awt.*;

public final class LabPanel {

    private LabPanel() {}

    public static JPanel vertical(int padding) {
        var panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(
                padding, padding, padding, padding
        ));
        return panel;
    }

    public static JPanel border(int padding) {
        var panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(
                padding, padding, padding, padding
        ));
        return panel;
    }
}
