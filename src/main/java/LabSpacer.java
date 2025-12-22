import javax.swing.*;

public final class LabSpacer {

    private LabSpacer() {}

    public static JComponent vertical(int px) {
        return new Box.Filler(
            new java.awt.Dimension(0, px),
            new java.awt.Dimension(0, px),
            new java.awt.Dimension(Short.MAX_VALUE, px)
        );
    }
}
