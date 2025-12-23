import com.fazecast.jSerialComm.SerialPort;

import javax.swing.SwingUtilities;
import java.util.stream.Stream;

void main() {
    var ports = SerialPort.getCommPorts();

    var portInfos = Stream.of(ports)
            .map(p -> new SerialPortInfo(
                    p.getSystemPortName(),
                    p.getDescriptivePortName()
            ))
            .toList();

    SwingUtilities.invokeLater(() -> MainWindow.show(portInfos));
}
