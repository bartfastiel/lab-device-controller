import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

public final class LabValueDisplay extends JComponent {

    private static final Color DISPLAY_BLUE = new Color(80, 160, 255);

    public static LabValueDisplay editable(int intDigits, int fracDigits) {
        return new LabValueDisplay(intDigits, fracDigits, true);
    }

    public static LabValueDisplay readonly(int intDigits, int fracDigits) {
        return new LabValueDisplay(intDigits, fracDigits, false);
    }

    private final int intDigits;
    private final int fracDigits;
    private final int dotIndex;

    private final StringBuilder buffer;
    private int cursor = -1;

    private BigDecimal min = BigDecimal.ZERO;
    private BigDecimal max;

    private Consumer<BigDecimal> onChange;

    private LabValueDisplay(int intDigits, int fracDigits, boolean editable) {
        this.intDigits = intDigits;
        this.fracDigits = fracDigits;
        this.dotIndex = intDigits;

        this.buffer = new StringBuilder(format(BigDecimal.ZERO));
        this.max = maxValue();

        setFont(new Font(Font.MONOSPACED, Font.BOLD, 56));
        setBackground(Color.BLACK);
        setForeground(DISPLAY_BLUE);

        setFocusable(editable);
        setFocusTraversalKeysEnabled(false);

        if (editable) {
            installKeyHandling();
            installMouseAndFocusHandling();
        }
    }

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
    // Mouse + focus: THE FIX
    // -------------------------

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        var fm = g2.getFontMetrics();
        int charW = fm.charWidth('0');
        int charH = fm.getAscent();

        int x = 10;
        int y = (getHeight() + charH) / 2 - 6;

        for (int i = 0; i < buffer.length(); i++) {

            // Cursor highlight
            if (i == cursor && hasFocus() && isFocusable()) {
                g2.setColor(Color.WHITE);
                g2.fillRect(x - 3, y - charH, charW + 6, fm.getHeight());
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(isEnabled() ? DISPLAY_BLUE : Color.GRAY);
            }

            // Digit
            g2.drawString(String.valueOf(buffer.charAt(i)), x, y);

            // Manual decimal dot (between intDigits and fracDigits)
            if (i + 1 == dotIndex) {
                drawDecimalDot(g2, x + charW, y, fm);
            }

            x += charW;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        var fm = getFontMetrics(getFont());

        int charW = fm.charWidth('0');
        int charH = fm.getHeight();

        int width =
                10                  // left padding
                        + charW * buffer.length()
                        + 10;                 // right padding

        int height =
                charH
                        + 10;                 // top/bottom padding

        return new Dimension(width, height);
    }

    private void drawDecimalDot(Graphics2D g2, int x, int baselineY, FontMetrics fm) {
        int radius = 5;

        int dotX = x - radius / 2;
        int dotY = baselineY - radius;

        g2.setColor(isEnabled() ? DISPLAY_BLUE : Color.GRAY);
        g2.fillOval(dotX, dotY, radius, radius);
    }

    private void installMouseAndFocusHandling() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;

                requestFocusInWindow();
                setCursorFromX(e.getX());
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                cursor = -1;
                repaint();
            }
        });
    }

    private void setCursorFromX(int x) {
        var fm = getFontMetrics(getFont());
        int charW = fm.charWidth('0');
        int startX = 10;

        int idx;
        idx = (x - startX + charW / 2) / charW;

        if (idx < 0) idx = 0;
        if (buffer.length() <= idx) idx = buffer.length() - 1;

        cursor = idx;
        repaint();
    }

    // -------------------------
    // Keys
    // -------------------------

    private void installKeyHandling() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isEnabled()) return;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> moveCursor(-1);
                    case KeyEvent.VK_RIGHT -> moveCursor(1);
                    case KeyEvent.VK_UP -> increment(1);
                    case KeyEvent.VK_DOWN -> increment(-1);
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (!isEnabled()) return;

                char c = e.getKeyChar();
                if ('0' <= c && c <= '9') {
                    replaceDigit(c);
                }
            }
        });
    }

    private void moveCursor(int dir) {
        if (cursor < 0) {
            cursor = 0;
            repaint();
            return;
        }

        int next;
        next = cursor + dir;

        if (0 <= next && next < buffer.length()) {
            cursor = next;
            repaint();
        }
    }

    private void replaceDigit(char c) {
        if (cursor < 0) return;

        buffer.setCharAt(cursor, c);
        emitChange();
        moveCursor(1);
    }

    private void increment(int delta) {
        if (cursor < 0) return;

        int posFromRight;
        posFromRight = (buffer.length() - 1) - cursor;

        BigDecimal step;
        step = BigDecimal.ONE.movePointLeft(posFromRight);

        BigDecimal next;
        next = clamp(toBigDecimal().add(step.multiply(BigDecimal.valueOf(delta))));

        setValue(next);
        emitChange();
    }

    private void emitChange() {
        if (onChange != null) onChange.accept(toBigDecimal());
    }

    private BigDecimal toBigDecimal() {
        var s = buffer.toString();
        var withDot = s.substring(0, dotIndex) + "." + s.substring(dotIndex);
        return new BigDecimal(withDot);
    }

    private BigDecimal clamp(BigDecimal v) {
        if (v.compareTo(min) < 0) return min.setScale(fracDigits, RoundingMode.UNNECESSARY);
        if (v.compareTo(max) > 0) return max;
        return v.setScale(fracDigits, RoundingMode.UNNECESSARY);
    }

    private String format(BigDecimal v) {
        BigDecimal scaled;
        scaled = v.setScale(fracDigits, RoundingMode.UNNECESSARY).movePointRight(fracDigits);

        long raw;
        raw = scaled.longValueExact();

        return String.format("%0" + (intDigits + fracDigits) + "d", raw);
    }

    private BigDecimal maxValue() {
        return BigDecimal.valueOf(Math.pow(10, intDigits))
                .subtract(BigDecimal.ONE.movePointLeft(fracDigits))
                .setScale(fracDigits, RoundingMode.UNNECESSARY);
    }
}
