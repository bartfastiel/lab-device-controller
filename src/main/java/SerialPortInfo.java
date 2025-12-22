
public record SerialPortInfo(
        String systemPortName,
        String description
) {
    @Override
    public String toString() {
        return description;
    }
}
