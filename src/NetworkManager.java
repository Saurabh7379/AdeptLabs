import java.util.*;

class Device {
    private String deviceType;
    private String deviceName;
    private ArrayList<Device> connections;
    private int strength = 1;

    public Device(String deviceType, String deviceName, int strength) {
        this.deviceType = deviceType;
        this.deviceName = deviceName;
        connections = new ArrayList<Device>();
        this.strength = strength;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public ArrayList<Device> getConnections() {
        return connections;
    }

    public int getStrength() {
        return strength;
    }

    public void addConnection(Device device) {
        if (!connections.contains(device)) {
            connections.add(device);
            device.addConnection(this);
        }
    }

    public boolean isComputer() {
        return "computer".equals(deviceType);
    }

    public boolean isRepeater() {
        return "repeater".equals(deviceType);
    }

}

class Network {
    private HashMap<String, Device> devices;

    public Network() {
        devices = new HashMap<String, Device>();
    }

    public boolean addDevice(String deviceType, String deviceName, int strength) {
        if (!devices.containsKey(deviceName)) {
            devices.put(deviceName, new Device(deviceType, deviceName, strength));
            return true;
        } else {
            return false;
        }
    }

    public void connectDevices(String deviceName, List<String> deviceList) {
        if (devices.containsKey(deviceName)) {
            Device currentDevice = devices.get(deviceName);
            for (String connectedDeviceName : deviceList) {
                if (devices.containsKey(connectedDeviceName)) {
                    Device connectedDevice = devices.get(connectedDeviceName);
                    currentDevice.addConnection(connectedDevice);
                } else {
                    System.out.println("Error: Device " + connectedDeviceName + " not found.");
                    return;
                }
            }
        } else {
            System.out.println("Error: Device " + deviceName + " not found.");
        }
    }

    public void printRoute(String srcDeviceName, String destDeviceName) {
        if (devices.containsKey(srcDeviceName) && devices.containsKey(destDeviceName)) {
            Device srcDevice = devices.get(srcDeviceName);
            Device destDevice = devices.get(destDeviceName);
            if (!srcDevice.isRepeater() && !destDevice.isRepeater()) {
                List<Device> route = findRoute(srcDevice, destDevice, new ArrayList<>(), srcDevice.getStrength());
                if (route != null) {
                    System.out.print("Route: ");
                    for (int i = 0; i < route.size() ;i++) {
                        System.out.print(route.get(i).getDeviceName() );
                        if(i != route.size() - 1){
                            System.out.print(" -> ");
                        }
                    }
                    System.out.println();
                } else {
                    System.out.println("Error: No route found between devices.");
                }
            } else {
                System.out.println("Error: Source or destination device cannot be a repeater.");
            }
        } else {
            System.out.println("Error: Source or destination device not found.");
        }
    }

    // Using Depth First Search for finding the route to the destination device
    private List<Device> findRoute(Device currentDevice, Device destDevice, List<Device> visited, int currentStrength) {
        if (currentDevice.equals(destDevice)) {
            visited.add(destDevice);
            return visited;
        }
        for (Device neighbor : currentDevice.getConnections()) {
            if (!visited.contains(neighbor) && currentStrength > 0 && !neighbor.equals(currentDevice)) {
                List<Device> newVisited = new ArrayList<>(visited);
                newVisited.add(currentDevice);

                // Decrease strength for each device, and double for repeaters
                int newStrength = currentDevice.isRepeater() ? 2 * currentStrength : currentStrength - 1;

                List<Device> route = findRoute(neighbor, destDevice, newVisited, newStrength);
                if (route != null) {
                    if(!route.contains(currentDevice)) {
                        route.add(0, currentDevice);
                    }
                    return route;
                }
            }
        }
        return null;
    }

    public void setDeviceStrength(String deviceName, int strength) {
        if (devices.containsKey(deviceName)) {
            Device device = devices.get(deviceName);
            if (!device.isRepeater()) {
                if (strength >= 0) {
                    device = new Device(device.getDeviceType(), device.getDeviceName(), strength);
                    devices.put(deviceName, device);
                } else {
                    System.out.println("Error: Strength cannot be negative.");
                }
            } else {
                System.out.println("Error: Strength cannot be defined for a repeater.");
            }
        } else {
            System.out.println("Error: Device " + deviceName + " not found.");
        }
    }
}

public class NetworkManager {
    public static void main(String[] args) {
        Network network = new Network();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter command: ");
            String[] command = scanner.nextLine().split(" ");
            if (command.length == 0) {
                continue;
            }

            switch (command[0].toUpperCase()) {
                case "ADD":
                    if (command.length == 3) {
                        String deviceType = command[1];
                        String deviceName = command[2];
                        if (network.addDevice(deviceType, deviceName, 5)) {
                            System.out.println("Device " + deviceName + " added to the network.");
                        } else {
                            System.out.println("Error: Device " + deviceName + " already exists. Please choose a unique name.");
                        }
                    } else {
                        System.out.println("Error: Invalid command. The correct syntax is : ADD <DEVICE_TYPE> <DEVICE_NAME>.");
                    }
                    break;

                case "CONNECT":
                    if (command.length >= 3) {
                        String deviceName = command[1];
                        List<String> deviceList = new ArrayList<>();
                        for (int i = 2; i < command.length; i++) {
                            deviceList.add(command[i]);
                        }
                        network.connectDevices(deviceName, deviceList);
                    } else {
                        System.out.println("Error: Invalid command. The correct syntax is: CONNECT <DEVICE_NAME> <DEVICE_LIST>");
                    }
                    break;

                case "INFO_ROUTE":
                    if (command.length == 3) {
                        String srcDeviceName = command[1];
                        String destDeviceName = command[2];
                        network.printRoute(srcDeviceName, destDeviceName);
                    } else {
                        System.out.println("Error: Invalid command. The correct syntax is: INFO_ROUTE <SRC_DEVICE_NAME> <DEST_DEVICE_NAME>");
                    }
                    break;

                case "SET_DEVICE_STRENGTH":
                    if (command.length == 3) {
                        String deviceName = command[1];
                        int strength = Integer.parseInt(command[2]);
                        network.setDeviceStrength(deviceName, strength);
                    } else {
                        System.out.println("Error: Invalid command. The correct syntax is: SET_DEVICE_STRENGTH <DEVICE_NAME> <#STRENGTH NUMBER>");
                    }
                    break;

                case "EXIT":
                    scanner.close();
                    System.exit(0);

                default:
                    System.out.println("Error: Invalid command. Please check the syntax.\n" +
                            "ADD <DEVICE_TYPE> <DEVICE_NAME>\n" +
                            "CONNECT <DEVICE_NAME> <DEVICE_LIST>\n" +
                            "INFO_ROUTE <SRC_DEVICE_NAME> <DEST_DEVICE_NAME>\n" +
                            "SET_DEVICE_STRENGTH <DEVICE_NAME> <#STRENGTH NUMBER>");
                    break;
            }
        }
    }
}
