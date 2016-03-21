package de.teyzer.genie.util;

public class Util {
    /**
     * Stellt sicher, dass der Wert zwischen 0 und 1 ist
     *
     * @param value Wert
     * @return Gekappter Wert
     */
    public static float cap01(float value) {
        return cap(value, 0, 1);
    }

    /**
     * Stellt sicher, dass der Wert zwischen min und max ist.
     *
     * @param value Wert
     * @param min   Min
     * @param max   Max
     * @return Gekappter Wert
     */
    public static float cap(float value, float min, float max) {
        return Math.min(Math.max(min, value), max);
    }
}
