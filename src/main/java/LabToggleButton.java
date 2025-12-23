import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public final class LabToggleButton extends JComponent {

    public static LabToggleButton create(String label) {
        return new LabToggleButton(label);
    }

    private final String label;
    private boolean on;
    private boolean hover;

    private Consumer<Boolean> onToggle;

    private LabToggleButton(String label) {
        this.label = label;

        setPreferredSize(new Dimension(120, 40));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onToggle != null) {
                    onToggle.accept(!on);
                }
            }
        });
    }

    public void onToggle(Consumer<Boolean> cb) {
        this.onToggle = cb;
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

        if (hover) {
            g2.setColor(Color.WHITE);
            g2.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
            g2.setColor(Color.BLACK);
        } else {
            g2.setColor(Color.WHITE);
        }

        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        g2.setFont(getFont().deriveFont(Font.BOLD, 14f));

        var text = label;
        var fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(text)) / 2;
        int ty = (getHeight() + fm.getAscent()) / 2 - 2;

        g2.drawString(text, tx, ty);

        // Status dot
        g2.setColor(on ? Color.RED : Color.DARK_GRAY);
        g2.fillOval(getWidth() - 16, 6, 10, 10);
    }
}
