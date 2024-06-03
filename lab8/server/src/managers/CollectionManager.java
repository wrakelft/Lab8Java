package managers;
import Collections.Vehicle;
import comparators.VehicleEnginePowerComparator;
import managers.dbLogic.PostgreSQLManager;
import system.Request;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Класс отвечает за взаимодействие с коллекцией на базовом уровне
 *
 * @see Vehicle
 * @author wrakelft
 * @since 1.0
 */

public class CollectionManager {

    private static Set<Vehicle> vehicleCollection;
    private final java.util.Date creationDate;
    private static CollectionManager instance;

    /**
     * Базовый конструктор
     *
     * @since 1.0
     */
    private CollectionManager() {
        vehicleCollection = new CopyOnWriteArraySet<>();
        creationDate = new java.sql.Date(new Date().getTime());
    }

    public static synchronized CollectionManager getInstance() {
        if (instance == null) {
            instance = new CollectionManager();
        }
        return instance;
    }

    public void loadCollectionFromDB() {
        synchronized (vehicleCollection) {
            PostgreSQLManager manager = new PostgreSQLManager();
            Set<Vehicle> vehicles = manager.getCollectionFromDB();
            vehicleCollection.clear();
            vehicleCollection.addAll(vehicles);
        }
        Logger.getLogger(CollectionManager.class.getName()).info("Collection reloaded from DB");
    }

    public void writeCollectionToDB() {
        PostgreSQLManager dbmanager = new PostgreSQLManager();
        dbmanager.writeCollectionToDB();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Получить коллекцию
     *
     * @return коллекция со всеми элементами
     */
    public Set<Vehicle> getVehicleCollection() {
        return vehicleCollection;
    }

    /**
     * Установить коллекцию
     *
     */
    public void setVehicleCollection(Set<Vehicle> newVehicleCollection) {
        vehicleCollection.clear();
        vehicleCollection.addAll(newVehicleCollection);
    }

    /**
     * Добавить элемент в коллекцию
     *
     */
    public static void addToCollection(Vehicle vehicle) {
        synchronized (vehicleCollection) {
            if(!vehicleCollection.contains(vehicle)) {
                vehicleCollection.add(vehicle);
                Logger.getLogger(CollectionManager.class.getName()).info("Vehicle was added" + vehicle);
            } else {
                Logger.getLogger(CollectionManager.class.getName()).warning("Attempt to add duplicate vehicle: " + vehicle);
            }
        }
    }


    /**
     * Показать все элементы коллекции
     *
     */
    public Set<Vehicle> showCollection() {
        return vehicleCollection;
    }


    public static List<Long> removeLower(Request request) {
        List<Long> lowerEPVehicleIds = new ArrayList<>();
        try {
            VehicleEnginePowerComparator vehicleEnginePowerComparator = new VehicleEnginePowerComparator();
            Set<Vehicle> vehicleCollection = CollectionManager.getInstance().getVehicleCollection();

            long inputEl = Long.parseLong(request.getKey());

            Vehicle referenceVehicle = vehicleCollection.stream()
                    .filter(vehicle -> vehicle.getId() == inputEl)
                    .findFirst()
                    .orElse(null);

            lowerEPVehicleIds = vehicleCollection.stream()
                    .filter(vehicle -> vehicleEnginePowerComparator.compare(vehicle, referenceVehicle) < 0)
                    .map(Vehicle::getId)
                    .collect(Collectors.toList());

            if (referenceVehicle == null) {
                return lowerEPVehicleIds;
            }

            if (lowerEPVehicleIds.isEmpty()) {
                return lowerEPVehicleIds;
            }

        }catch (NumberFormatException e) {
            return lowerEPVehicleIds;
        }catch (NullPointerException e) {
            return lowerEPVehicleIds;
        }
        return lowerEPVehicleIds;
    }

    public static String GroupCountingByCreationDate(Request request) {
        Set<Vehicle> vehicleCollection = CollectionManager.getInstance().getVehicleCollection();
        if (vehicleCollection.isEmpty()) {
            return "Collection is empty";
        } else {
            Map<Date, Long> groupedByCreatioonDate = vehicleCollection.stream()
                    .collect(Collectors.groupingBy(Vehicle::getCreationDate,Collectors.counting()));
            String result = groupedByCreatioonDate.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining("\n"));
            return result;
        }
    }


    public static String countByFuelType(Request request) {
        Set<Vehicle> vehicleCollection = CollectionManager.getInstance().getVehicleCollection();
        String enterFuelType = request.getKey();

        long count = vehicleCollection.stream()
                .filter(vehicle -> vehicle.getFuelType().toString().equalsIgnoreCase(enterFuelType))
                .count();
        return ("Coincidences: " + count);
    }

    public static String countLessThenFuelType(Request request) {
        Set<Vehicle> vehicleCollection = CollectionManager.getInstance().getVehicleCollection();
        String enterFuelType = request.getKey();

        long count = vehicleCollection.stream()
                .filter(vehicle -> !(vehicle.getFuelType().toString().equalsIgnoreCase(enterFuelType)))
                .count();
        return ("Do not match: " + count);
    }

    public static String addIfMax(Request request) {
        VehicleEnginePowerComparator comparator = new VehicleEnginePowerComparator();
        if(vehicleCollection.isEmpty() || vehicleCollection.stream().allMatch(vehicle -> comparator.compare(vehicle,request.getVehicle()) < 0)) {
            PostgreSQLManager manager = new PostgreSQLManager();
            Vehicle obj = request.getVehicle();
            obj.setCreationDate(new Date());
            long generatedId = manager.writeObjToDB(obj);
            if (generatedId != -1) {
                CollectionManager.getInstance().loadCollectionFromDB();
            }
        } else {
            return "new vehicle has lower engine power!";
        }
        return "Success! Vehicle was added";
    }

}
