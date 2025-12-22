import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    public MainWindow(List<SerialPortInfo> ports) {
        setTitle("Lab Device Controller");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(Color.BLACK);
        getContentPane().setLayout(new BorderLayout());

        add(createPortListPanel(ports), BorderLayout.CENTER);

        setVisible(true);
    }

    private JComponent createPortListPanel(List<SerialPortInfo> ports) {
        var panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        var title = new JLabel("Available serial devices:");
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(title);

        if (ports.isEmpty()) {
            var label = new JLabel("No supported serial devices found.");
            label.setForeground(Color.RED);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(label);
            return panel;
        }

        for (var port : ports) {
            var button = new JButton(port.toString());
            button.setAlignmentX(Component.LEFT_ALIGNMENT);

            button.addActionListener(e ->
                    System.out.println("Selected port: " + port.systemPortName())
            );

            panel.add(button);
            panel.add(Box.createVerticalStrut(8));
        }

        return panel;
    }
}
