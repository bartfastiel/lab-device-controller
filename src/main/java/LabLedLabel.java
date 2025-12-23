import javax.swing.*;
import java.awt.*;

public final class LabLedLabel extends JComponent {

    public static LabLedLabel create() {
        return new LabLedLabel();
    }

    private Color color = Color.DARK_GRAY;

    private LabLedLabel() {
        setPreferredSize(new Dimension(14, 14));
    }

    public void setOn(boolean on) {
        this.color = on ? Color.RED : Color.DARK_GRAY;
        repaint();
    }

    public void setGreen() {
        this.color = Color.GREEN;
        repaint();
    }

    public void setGray() {
        this.color = Color.DARK_GRAY;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(color);
        g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
    }
}
