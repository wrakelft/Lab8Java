package managers.dbLogic;

import Collections.Vehicle;

import java.util.HashSet;
import java.util.Set;

public interface DBmanager {
    Set<Vehicle> getCollectionFromDB();

    void writeCollectionToDB();
}
