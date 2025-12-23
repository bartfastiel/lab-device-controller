import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

public final class LabValueDisplay extends JComponent {

    // -------------------------
    // Factory
    // -------------------------

    public static LabValueDisplay editable(int intDigits, int fracDigits) {
        return new LabValueDisplay(intDigits, fracDigits, true);
    }

    public static LabValueDisplay readonly(int intDigits, int fracDigits) {
        return new LabValueDisplay(intDigits, fracDigits, false);
    }

    // -------------------------
    // Configuration
    // -------------------------

    private final int intDigits;
    private final int fracDigits;
    private final boolean editable;

    // -------------------------
    // State
    // -------------------------

    private final StringBuilder buffer;
    private int cursor = -1;

    private BigDecimal min = BigDecimal.ZERO;
    private BigDecimal max;

    private Consumer<BigDecimal> onChange;

    // -------------------------
    // Construction
    // -------------------------

    private LabValueDisplay(int intDigits, int fracDigits, boolean editable) {
        this.intDigits = intDigits;
        this.fracDigits = fracDigits;
        this.editable = editable;

        this.buffer = new StringBuilder(initialText());

        this.max = maxValue();

        setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        setBackground(Color.BLACK);
        setForeground(new Color(0, 0, 255));
        setFocusable(editable);

        if (editable) installKeyHandling();
    }

    // -------------------------
    // Public API
    // -------------------------

    public void setValue(BigDecimal value) {
        var clamped = clamp(value);
        var s = format(clamped);

        buffer.setLength(0);
        buffer.append(s);

        repaint();
    }

    public void onChange(Consumer<BigDecimal> listener) {
        this.onChange = listener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        repaint();
    }

    // -------------------------
    // Rendering
    // -------------------------

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(isEnabled() ? getForeground() : Color.GRAY);
        var fm = g2.getFontMetrics();

        int x = 10;
        int y = (getHeight() + fm.getAscent()) / 2 - 4;

        for (int i = 0; i < buffer.length(); i++) {
            char c = buffer.charAt(i);

            if (i == cursor && hasFocus() && editable) {
                g2.setColor(Color.WHITE);
                g2.fillRect(x - 2, y - fm.getAscent(), fm.charWidth(c) + 4, fm.getHeight());
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(isEnabled() ? getForeground() : Color.GRAY);
            }

            g2.drawString(String.valueOf(c), x, y);
            x += fm.charWidth(c);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        var fm = getFontMetrics(getFont());
        return new Dimension(
                fm.charWidth('0') * buffer.length() + 20,
                fm.getHeight() + 10
        );
    }

    // -------------------------
    // Key handling
    // -------------------------

    private void installKeyHandling() {
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (!isEnabled()) return;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT  -> moveCursor(-1);
                    case KeyEvent.VK_RIGHT -> moveCursor(1);
                    case KeyEvent.VK_UP    -> increment(1);
                    case KeyEvent.VK_DOWN  -> increment(-1);
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (!isEnabled()) return;

                char c = e.getKeyChar();
                if (c >= '0' && c <= '9') {
                    replaceDigit(c);
                }
            }
        });
    }

    private void moveCursor(int dir) {
        if (cursor < 0) cursor = firstDigit();
        else {
            int next = cursor + dir;
            if (next >= 0 && next < buffer.length() && buffer.charAt(next) != '.') {
                cursor = next;
            }
        }
        repaint();
    }

    private void replaceDigit(char c) {
        if (cursor < 0 || buffer.charAt(cursor) == '.') return;

        buffer.setCharAt(cursor, c);
        emitChange();
        moveCursor(1);
    }

    private void increment(int delta) {
        if (cursor < 0 || buffer.charAt(cursor) == '.') return;

        int posFromRight = (buffer.length() - 1) - cursor;
        if (buffer.charAt(buffer.length() - 1 - posFromRight) == '.') posFromRight--;

        BigDecimal step = BigDecimal.ONE.movePointLeft(posFromRight);
        BigDecimal next = clamp(toBigDecimal().add(step.multiply(BigDecimal.valueOf(delta))));

        setValue(next);
        emitChange();
    }

    // -------------------------
    // Helpers
    // -------------------------

    private void emitChange() {
        if (onChange != null) onChange.accept(toBigDecimal());
    }

    private BigDecimal toBigDecimal() {
        return new BigDecimal(buffer.toString());
    }

    private BigDecimal clamp(BigDecimal v) {
        if (v.compareTo(min) < 0) return min;
        if (v.compareTo(max) > 0) return max;
        return v.setScale(fracDigits, RoundingMode.UNNECESSARY);
    }

    private String format(BigDecimal v) {
        return String.format(
                "%0" + intDigits + "d.%0" + fracDigits + "d",
                v.intValue(),
                v.remainder(BigDecimal.ONE)
                        .movePointRight(fracDigits)
                        .abs()
                        .intValue()
        );
    }

    private String initialText() {
        return format(BigDecimal.ZERO);
    }

    private BigDecimal maxValue() {
        return BigDecimal.valueOf(Math.pow(10, intDigits))
                .subtract(BigDecimal.ONE.movePointLeft(fracDigits));
    }

    private int firstDigit() {
        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) != '.') return i;
        }
        return -1;
    }
}
