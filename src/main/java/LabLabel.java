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

    public static Component title(String title) {
        return create(title);
    }

    public static Component small(String cv) {
        return create(cv);
    }

    public static Component normal(String label) {
        return create(label);
    }
}
