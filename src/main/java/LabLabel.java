import javax.swing.*;
import java.awt.*;

public final class LabLabel {

    private LabLabel() {}

    public static JLabel create(String text) {
        var label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
