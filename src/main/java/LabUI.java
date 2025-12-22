import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class LabUI {

    private static final Color BG = Color.BLACK;
    private static final Color FG = Color.WHITE;
    private static final Border BORDER = BorderFactory.createLineBorder(Color.WHITE);

    private LabUI() {
    }

    public static JPanel panel() {
        var panel = new JPanel();
        panel.setBackground(BG);
        return panel;
    }

    public static JPanel verticalPanel(int padding) {
        var panel = panel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(
                padding, padding, padding, padding
        ));
        return panel;
    }

    public static JLabel label(String text) {
        var label = new JLabel(text);
        label.setForeground(FG);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JButton button(String text) {
        var button = new JButton(text);

        button.setForeground(FG);
        button.setBackground(BG);
        button.setBorder(BORDER);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(FG);
                button.setForeground(BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BG);
                button.setForeground(FG);
            }
        });

        return button;
    }

    public static Component vSpace(int px) {
        return Box.createVerticalStrut(px);
    }
}
