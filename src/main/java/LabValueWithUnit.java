import javax.swing.*;
import java.awt.*;

public final class LabValueWithUnit extends JPanel {

    private static final Color DISPLAY_BLUE = new Color(80, 160, 255);

    private LabValueWithUnit(LabValueDisplay value, String unit) {
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        var unitLabel = new JLabel(unit);
        unitLabel.setForeground(DISPLAY_BLUE);
        unitLabel.setFont(
                value.getFont().deriveFont(value.getFont().getSize2D() * 0.3f)
        );

        var gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE;

        // value (big) - baseline aligned
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        add(value, gbc);

        // unit (small) - baseline aligned
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        add(unitLabel, gbc);
    }

    public static LabValueWithUnit of(LabValueDisplay value, String unit) {
        return new LabValueWithUnit(value, unit);
    }
}
