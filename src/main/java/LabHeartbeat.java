import javax.swing.*;
import java.awt.*;

public final class LabHeartbeat extends JComponent {

    public static LabHeartbeat create() {
        return new LabHeartbeat();
    }

    private boolean on;

    private LabHeartbeat() {
        setPreferredSize(new Dimension(14, 14));
    }

    public void setOn(boolean on) {
        this.on = on;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(on ? Color.WHITE : Color.BLACK);

        // Kreis (wenn du lieber ein Rechteck willst: fillRect)
        g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);

        // Rahmen für Sichtbarkeit
        g2.setColor(Color.WHITE);
        g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
    }
}
