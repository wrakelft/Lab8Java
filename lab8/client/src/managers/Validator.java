package managers;

import Collections.FuelType;
import Collections.Vehicle;
import Collections.VehicleType;
import exceptions.WrongArgumentException;

import java.util.HashSet;

/**
 * Класс для проверки на валидность полей объекта Vehicle и для проверки входных данных из файла
 *
 * @author wrakelft
 * @since 1.0
 */
public class Validator {
    HashSet<Vehicle> vehicleList;

    public Validator(HashSet<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    /**
     * Проверяет, что значение строки не null
     *
     * @param arg  аргумент строки
     * @param data что из Organization мы проверяем
     * @throws WrongArgumentException если значение arg null
     */
    public static boolean inputNotEmpty(String arg) {
        if (arg.isEmpty() || arg.trim().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Проверяет корректность координат по X
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если координата некорректна
     * @see Collections.Coordinates
     */
    public static boolean xGood(long arg) {
        long v = arg;
        if (v <= -952 || (v >= 9223372036854775807l)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Проверяет корректность координат по Y
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если координата некорректна
     * @see Collections.Coordinates
     */
    public static boolean yGood(int arg) {
        int v = arg;
        if ((v <= -109) || (v >= 2147483647)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Проверяет корректность значения engine power
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если некорректно
     * @see Vehicle
     */
    public static boolean enginePowerGood(Integer arg) {
        int v = arg;
        if (v <= 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Проверяет корректность значения Vehicle type
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если некорректно
     * @see VehicleType
     */
    public static boolean typeGood(String arg) {
        try {
            VehicleType.valueOf(arg.toUpperCase());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Проверяет корректность значения Fuel Type
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если некорректно
     * @see FuelType
     */
    public static boolean fuelTypeGood(String arg) {
        try {
            FuelType.valueOf(arg.toUpperCase());
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}