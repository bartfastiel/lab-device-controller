import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MainWindow extends JFrame {

    public MainWindow(List<SerialPortInfo> ports) {
        setTitle("Lab Device Controller");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var content = getContentPane();
        content.setBackground(Color.BLACK);
        content.setLayout(new BorderLayout());

        content.add(createPortListPanel(ports), BorderLayout.CENTER);

        setVisible(true);
    }

    private JComponent createPortListPanel(List<SerialPortInfo> ports) {
        var panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        );

        if (ports.isEmpty()) {
            panel.add(createLabel("Please plug in a serial device and restart the application."));
            return panel;
        }

        panel.add(createLabel("Select the device:"));
        panel.add(Box.createVerticalStrut(25));

        for (var port : ports) {
            var button = createButton(port.toString());
            button.addActionListener(_ ->
                    System.out.println("Selected device: " + port.systemPortName())
            );

            panel.add(button);
            panel.add(Box.createVerticalStrut(15));
        }

        return panel;
    }

    private JButton createButton(String text) {
        var button = new JButton(text);

        button.setForeground(Color.WHITE);
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setMaximumSize(new Dimension(
                Integer.MAX_VALUE,
                40
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.BLACK);
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }

    private JLabel createLabel(String text) {
        var label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
