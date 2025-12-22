import com.fazecast.jSerialComm.SerialPort;

import javax.swing.SwingUtilities;
import java.util.stream.Stream;

void main() {
    var ports = SerialPort.getCommPorts();

    var portInfos = Stream.of(ports)
            .map(port -> new SerialPortInfo(
                    port.getSystemPortName(),
                    port.getDescriptivePortName()
            ))
            .toList();

    SwingUtilities.invokeLater(() -> new MainWindow(portInfos));
}
