import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;
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
    private final int digits;
    private final int dotIndex;

    private AtomicLong currentDisplayValue = new AtomicLong(0);
    private int cursor = -1;

    private Consumer<BigDecimal> onChange;

    private LabValueDisplay(int intDigits, int fracDigits, boolean editable) {
        this.intDigits = intDigits;
        this.fracDigits = fracDigits;
        this.digits = intDigits + fracDigits;
        this.dotIndex = intDigits;

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
        currentDisplayValue.set(value
                .setScale(fracDigits, RoundingMode.UNNECESSARY)
                .movePointRight(fracDigits)
                .intValueExact());

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

        // now loop over digits, by using the ten-exponent of every place in currentDisplayValue
        var v = currentDisplayValue.get();
        for (int characterIndex = 0; characterIndex < digits; characterIndex++) {
            int tenExp = digits - characterIndex - 1;
            char c = (char) ('0' + v / Math.pow(10, tenExp) % 10);

            // Cursor highlight
            if (characterIndex == cursor && hasFocus() && isFocusable()) {
                g2.setColor(Color.WHITE);
                g2.fillRect(x - 3, y - charH, charW + 6, fm.getHeight());
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(isEnabled() ? DISPLAY_BLUE : Color.GRAY);
            }

            // Digit
            g2.drawString(String.valueOf(c), x, y);

            x += charW;
        }

        // Decimal dot
        drawDecimalDot(g2, 10 + charW * dotIndex, y, fm);
    }

    @Override
    public Dimension getPreferredSize() {
        var fm = getFontMetrics(getFont());

        int charW = fm.charWidth('0');
        int charH = fm.getHeight();

        int width = charW * digits + charW / 2 + 10;

        int height = charH;

        return new Dimension(width, height);
    }

    @Override
    public int getBaseline(int width, int height) {
        var fm = getFontMetrics(getFont());

        // baseline = top padding + ascent
        int charH = fm.getAscent();

        // must match paintComponent vertical placement
        return (height + charH) / 2 - 6;
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return BaselineResizeBehavior.CONSTANT_ASCENT;
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

        int relativeX = x - startX;
        int charIndex = relativeX / charW;
        cursor = Math.max(0, Math.min(digits - 1, charIndex));

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
        var clamped = Math.max(0, Math.min(digits - 1, cursor + dir));
        if (clamped != cursor) {
            cursor = clamped;
            repaint();
        }
    }

    private void replaceDigit(char c) {
        if (cursor < 0) return;
        var currentValue = currentDisplayValue.get();
        int tenExp = digits - 1 - cursor;
        long placeValue = (long) Math.pow(10, tenExp);
        long digitValue = Math.max(0, Math.min(9, c - '0')) * placeValue;
        long newValue = currentValue - currentValue / placeValue % 10 * placeValue + digitValue;
        currentDisplayValue.set(clamp(newValue));
        emitChange();
        moveCursor(1);
        repaint();
    }

    private void increment(int delta) {
        if (cursor < 0) return;
        var currentValue = currentDisplayValue.get();
        int tenExp = digits - 1 - cursor;
        long placeValue = (long) Math.pow(10, tenExp - fracDigits);
        long newValue = currentValue + delta * placeValue;
        currentDisplayValue.set(clamp(newValue));
        emitChange();
        repaint();
    }

    private void emitChange() {
        if (onChange != null) onChange.accept(toBigDecimal());
    }

    private BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(currentDisplayValue.get())
                .movePointLeft(fracDigits)
                .setScale(fracDigits, RoundingMode.UNNECESSARY);
    }

    private long clamp(long v) {
        if (v < 0) return 0;
        if (v >= Math.pow(10, digits)) {
            return (int) Math.pow(10, digits) - 1;
        }
        return v;
    }
}
