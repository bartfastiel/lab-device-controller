import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.*;

public final class DeviceOverviewView {

    private DeviceOverviewView() {}

    public static JComponent createView(
            SerialPortInfo device,
            Runnable onBack
    ) {
        var root = LabPanel.border(30);

        var topButton = LabButton.create(device.toString());
        topButton.addActionListener(_ -> onBack.run());
        root.add(topButton, BorderLayout.NORTH);

        var statusLabel = LabLabel.create("Connecting...");
        root.add(statusLabel, BorderLayout.CENTER);

        // ---- Serial communication in background thread ----
        new Thread(() -> {
            var port = SerialPort.getCommPort(device.systemPortName());

            port.setBaudRate(9600);
            port.setNumDataBits(8);
            port.setNumStopBits(SerialPort.ONE_STOP_BIT);
            port.setParity(SerialPort.NO_PARITY);

            port.setComPortTimeouts(
                    SerialPort.TIMEOUT_READ_BLOCKING,
                    1000,
                    0
            );

            if (!port.openPort()) {
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Error: Unable to open port")
                );
                return;
            }

            try {
                byte[] readAllCmd = new byte[] {
                        (byte) 0xF7,
                        (byte) 0x02,
                        (byte) 0x03,
                        (byte) 0x04,
                        (byte) 0x09,
                        (byte) 0xE2,
                        (byte) 0xAB,
                        (byte) 0xFD
                };

                port.writeBytes(readAllCmd, readAllCmd.length);

                byte[] buffer = new byte[256];
                int len = port.readBytes(buffer, buffer.length);

                if (len <= 0) {
                    SwingUtilities.invokeLater(() ->
                            statusLabel.setText("No response from device")
                    );
                    return;
                }

                var responseHex = toHex(buffer, len);

                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Response: " + responseHex)
                );

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Error: " + e.getMessage())
                );
            } finally {
                port.closePort();
            }
        }, "device-test-thread").start();

        return root;
    }

    private static String toHex(byte[] data, int len) {
        var sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02X ", data[i]));
        }
        return sb.toString().trim();
    }
}
