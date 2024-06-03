package system;

import Collections.Vehicle;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class Request implements Serializable {
    private static final long serialVersionUID = 5266595875730820760L;
    private String message = null;
    private Vehicle vehicle = null;

    private String key = null;

    private String name = null;
    private char[] passwd = null;
    private Set<Vehicle> vehicles = null;
    private List<Long> ids = null;

    public Request(String message, Vehicle vehicle, String key, String name, char[] passwd, Set<Vehicle> vehicles, List<Long> ids) {
        this.message = message;
        this.vehicle = vehicle;
        this.key = key;
        this.name = name;
        this.passwd = passwd;
        this.vehicles = vehicles;
        this.ids = ids;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char[] getPasswd() {
        return passwd;
    }

    public void setPasswd(char[] passwd) {
        this.passwd = passwd;
    }

    public Set<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(Set<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
