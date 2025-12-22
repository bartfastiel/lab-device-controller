import javax.swing.JFrame;
import java.awt.Color;

public class MainWindow extends JFrame {
    public MainWindow() {
        setTitle("Lab Device Controller");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        setVisible(true);
    }
}
