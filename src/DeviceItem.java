public class DeviceItem {
    public String name;
    public int device;

    public DeviceItem(String name, int device) {
        this.name = name;
        this.device = device;
    }
    @Override
    public String toString() { return name; }
}
