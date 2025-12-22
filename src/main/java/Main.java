import com.fazecast.jSerialComm.*;

import javax.swing.SwingUtilities;

void main() {
    var ports = SerialPort.getCommPorts();
    if (ports.length == 0) {
        System.out.println("No serial ports found.");
        return;
    }
    Stream.of(ports).forEach(port ->
        System.out.println("Found port: " + port.getSystemPortName() + " - " + port.getDescriptivePortName())
    );

    SwingUtilities.invokeLater(MainWindow::new);
}
