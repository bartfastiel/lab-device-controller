import javax.swing.*;
import java.awt.*;

public final class DeviceOverviewView {

    private DeviceOverviewView() {
    }

    public static OverviewScreen createView(
            SerialPortInfo device,
            DeviceActions actions,
            Runnable onBack
    ) {
        var root = LabPanel.border(30);

        // -------------------------
        // Top bar
        // -------------------------

        var top = new JPanel(new BorderLayout());
        top.setBackground(Color.BLACK);

        var backButton = LabButton.create(device.toString());
        backButton.addActionListener(_ -> onBack.run());

        var statusLed = LabLedLabel.create(); // gray by default
        var heartbeat = LabHeartbeat.create();    // white/black toggling

        top.add(backButton, BorderLayout.WEST);

        var right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.BLACK);
        right.add(heartbeat);
        right.add(statusLed);
        top.add(right, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);

        // -------------------------
        // Center layout
        // -------------------------

        var center = new JPanel(new GridLayout(1, 2, 40, 0));
        center.setBackground(Color.BLACK);

        // Channel 1
        var ch1VoltageSet = LabValueDisplay.editable(2, 2);
        var ch1CurrentSet = LabValueDisplay.editable(1, 3);
        var ch1VoltageMeas = LabValueDisplay.readonly(2, 2);
        var ch1CurrentMeas = LabValueDisplay.readonly(1, 3);

        var ch1CV = LabLedLabel.create();
        var ch1CC = LabLedLabel.create();

        center.add(
                channelPanel(
                        "Channel 1",
                        ch1VoltageSet,
                        ch1VoltageMeas,
                        ch1CurrentSet,
                        ch1CurrentMeas,
                        ch1CV,
                        ch1CC
                )
        );

        // Channel 2
        var ch2VoltageSet = LabValueDisplay.editable(2, 2);
        var ch2CurrentSet = LabValueDisplay.editable(1, 3);
        var ch2VoltageMeas = LabValueDisplay.readonly(2, 2);
        var ch2CurrentMeas = LabValueDisplay.readonly(1, 3);

        var ch2CV = LabLedLabel.create();
        var ch2CC = LabLedLabel.create();

        center.add(
                channelPanel(
                        "Channel 2",
                        ch2VoltageSet,
                        ch2VoltageMeas,
                        ch2CurrentSet,
                        ch2CurrentMeas,
                        ch2CV,
                        ch2CC
                )
        );

        root.add(center, BorderLayout.CENTER);

        // -------------------------
        // Bottom buttons
        // -------------------------

        var bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        bottom.setBackground(Color.BLACK);

        var outputBtn = LabToggleButton.create("Output");
        var serialBtn = LabToggleButton.create("Serial");
        var parallelBtn = LabToggleButton.create("Parallel");

        outputBtn.onToggle(actions.setOutput());
        serialBtn.onToggle(actions.setSerial());
        parallelBtn.onToggle(actions.setParallel());

        bottom.add(outputBtn);
        bottom.add(serialBtn);
        bottom.add(parallelBtn);

        root.add(bottom, BorderLayout.SOUTH);

        // -------------------------
        // Wire UI -> Device actions
        // -------------------------

        ch1VoltageSet.onChange(actions.setCh1Voltage());
        ch1CurrentSet.onChange(actions.setCh1Current());

        ch2VoltageSet.onChange(actions.setCh2Voltage());
        ch2CurrentSet.onChange(actions.setCh2Current());

        // -------------------------
        // Bindings (Device -> UI)
        // -------------------------

        var bindings = new OverviewBindings(
                statusLed::setGray,
                statusLed::setGreen,
                heartbeat::setOn,

                ch1VoltageMeas::setValue,
                ch1CurrentMeas::setValue,
                ch1VoltageSet::setValue,
                ch1CurrentSet::setValue,
                ch1CV::setOn,
                ch1CC::setOn,

                ch2VoltageMeas::setValue,
                ch2CurrentMeas::setValue,
                ch2VoltageSet::setValue,
                ch2CurrentSet::setValue,
                ch2CV::setOn,
                ch2CC::setOn,

                outputBtn::setOn,
                serialBtn::setOn,
                parallelBtn::setOn
        );

        return new OverviewScreen(root, bindings);
    }

    // -------------------------
    // Helper: channel layout
    // -------------------------

    private static JComponent channelPanel(
            String title,
            LabValueDisplay vSet,
            LabValueDisplay vMeas,
            LabValueDisplay iSet,
            LabValueDisplay iMeas,
            LabLedLabel cv,
            LabLedLabel cc
    ) {
        var p = new JPanel();
        p.setBackground(Color.BLACK);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(LabLabel.title(title));
        p.add(Box.createVerticalStrut(20));

        p.add(row("Voltage", vSet, vMeas));
        p.add(Box.createVerticalStrut(10));
        p.add(row("Current", iSet, iMeas));
        p.add(Box.createVerticalStrut(10));

        var status = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        status.setBackground(Color.BLACK);
        status.add(LabLabel.small("CV"));
        status.add(cv);
        status.add(LabLabel.small("CC"));
        status.add(cc);

        p.add(status);
        return p;
    }

    private static JComponent row(String label, JComponent left, JComponent right) {
        var p = new JPanel(new GridLayout(1, 3, 10, 0));
        p.setBackground(Color.BLACK);
        p.add(LabLabel.normal(label));
        p.add(left);
        p.add(right);
        return p;
    }
}
