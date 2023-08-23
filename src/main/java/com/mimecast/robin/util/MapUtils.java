package com.mimecast.robin.util;

import java.util.List;
import java.util.Map;

/**
 * Map utils.
 */
@SuppressWarnings("unchecked")
public class MapUtils {

    /**
     * Flatten map.
     *
     * @param map        Map of String, Object instance to flatten.
     * @param precedence Preceding precedence.
     * @param collector  List of string to collect results.
     */
    public static void flattenMap(Map<String, Object> map, String precedence, List<String> collector) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            flattenObject(entry.getValue(), precedence + entry.getKey() + ">", collector);
        }
    }

    /**
     * Flatten list.
     *
     * @param list       List of String instance to flatten.
     * @param precedence Preceding precedence.
     * @param collector  List of string to collect results.
     */
    public static void flattenList(List<String> list, String precedence, List<String> collector) {
        for (int i = 0; i < list.size(); i++) {
            flattenObject(list.get(i), precedence + String.valueOf(i) + ">", collector);
        }
    }

    /**
     * Flatten object.
     *
     * @param object     Object instance to flatten.
     * @param precedence Preceding precedence.
     * @param collector  List of string to collect results.
     */
    @SuppressWarnings("unchecked")
    protected static void flattenObject(Object object, String precedence, List<String> collector) {
        if (object instanceof Map) {
            flattenMap((Map<String, Object>) object, precedence, collector);

        } else {
            String endPrecedence = precedence.replaceAll(">$", ": ");

            if (object instanceof List) {
                if (!((List<?>) object).isEmpty()) {
                    flattenList((List) object, precedence, collector);

                } else {
                    collector.add(endPrecedence + String.valueOf(object));
                }

            } else {
                collector.add(endPrecedence + String.valueOf(object));
            }
        }
    }
}
