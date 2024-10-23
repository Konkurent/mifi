package org.example.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CollectionUtils {

    private CollectionUtils() {}

    public static class ModifiableList {

        private ModifiableList() {}

        @SafeVarargs
        public static <T> List<T> of(Collection<T>... collections) {
            if (collections == null || collections.length == 0) return new ArrayList<>();
            int len = Arrays.stream(collections).map(Collection::size).reduce(0, Integer::sum);
            if (len == 0) return new ArrayList<>();
            List<T> result = new ArrayList<>(len);
            for (Collection<T> collection : collections) {
                result.addAll(collection);
            }
            return result;
        }
    }
}
