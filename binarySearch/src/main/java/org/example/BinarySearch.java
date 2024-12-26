package org.example;

import java.util.Comparator;
import java.util.List;

public class BinarySearch {

    private BinarySearch() {}

    public static class Arrays {

        private Arrays() {}

        private static void checkRange(int arrayLength, int fromIndex, int toIndex) {
            if (fromIndex > toIndex) {
                throw new IllegalArgumentException( "fromIndex [%s] > toIndex [%s]".formatted(fromIndex, toIndex));
            }
            if (fromIndex < 0) {
                throw new ArrayIndexOutOfBoundsException(fromIndex);
            }
            if (toIndex > arrayLength) {
                throw new ArrayIndexOutOfBoundsException(toIndex);
            }
        }

        //  ------------------ byte ------------------>
        public static int binarySearch(byte[] a, byte key) {
            return binarySearch(a, 0, a.length, key);
        }

        public static int binarySearch(byte[] a, int fromIndex, int toIndex, byte key) {
            checkRange(a.length, fromIndex, toIndex);
            int l = fromIndex;
            int r = toIndex - 1;

            while (l <= r) {
                int mid = (l + r) / 2;
                byte midVal = a[mid];

                if (midVal < key) {
                    l = mid + 1;
                }
                else if (midVal > key) {
                    r = mid - 1;
                }
                else {
                    return mid;
                }
            }
            return -1;
        }

        //  ------------------ byte ------------------/

        //  ------------------ short ------------------>
        public static int binarySearch(short[] a, short key) {
            return binarySearch(a, 0, a.length, key);
        }

        public static int binarySearch(short[] a, int fromIndex, int toIndex, short key) {
            checkRange(a.length, fromIndex, toIndex);
            int l = fromIndex;
            int r = toIndex - 1;

            while (l <= r) {
                int mid = (l + r) / 2;
                short midVal = a[mid];

                if (midVal < key) {
                    l = mid + 1;
                }
                else if (midVal > key) {
                    r = mid - 1;
                }
                else {
                    return mid;
                }
            }
            return -1;
        }
        //  ------------------ short ------------------/

        //  ------------------ char ------------------>
        public static int binarySearch(char[] a, char key) {
            return binarySearch(a, 0, a.length, key);
        }

        public static int binarySearch(char[] a, int fromIndex, int toIndex, char key) {
            checkRange(a.length, fromIndex, toIndex);
            int l = fromIndex;
            int r = toIndex - 1;

            while (l <= r) {
                int mid = (l + r) / 2;
                char midVal = a[mid];

                if (midVal < key)
                    l = mid + 1;
                else if (midVal > key)
                    r = mid - 1;
                else
                    return mid;
            }
            return -1;
        }
        //  ------------------ char ------------------/

        //  ------------------ int ------------------>
        public static int binarySearch(int[] a, int key) {
            return binarySearch(a, 0, a.length, key);
        }

        public static int binarySearch(int[] a, int fromIndex, int toIndex, int key) {
            checkRange(a.length, fromIndex, toIndex);
            int l = fromIndex;
            int r = toIndex - 1;

            while (l <= r) {
                int mid = (l + r) / 2;
                int midVal = a[mid];

                if (midVal < key)
                    l = mid + 1;
                else if (midVal > key)
                    r = mid - 1;
                else
                    return mid;
            }
            return -1;
        }
        //  ------------------ int ------------------/

        //  ------------------ long ------------------>
        public static int binarySearch(long[] a, long key) {
            return binarySearch(a, 0, a.length, key);
        }

        public static int binarySearch(long[] a, int fromIndex, int toIndex, long key) {
            checkRange(a.length, fromIndex, toIndex);
            int l = fromIndex;
            int r = toIndex - 1;

            while (l <= r) {
                int mid = (l + r) / 2;
                long midVal = a[mid];

                if (midVal < key)
                    l = mid + 1;
                else if (midVal > key)
                    r = mid - 1;
                else
                    return mid;
            }
            return -1;
        }
        //  ------------------ long ------------------/

        //  ------------------ double ------------------>
        public static int binarySearch(double[] a, double key) {
            return binarySearch(a, 0, a.length, key);
        }

        public static int binarySearch(double[] a, int fromIndex, int toIndex, double key) {
            checkRange(a.length, fromIndex, toIndex);
            int l = fromIndex;
            int r = toIndex - 1;

            while (l <= r) {
                int mid = (l + r) / 2;
                double midVal = a[mid];

                if (midVal < key) {
                    l = mid + 1;
                }
                else if (midVal > key) {
                    r = mid - 1;
                }
                else {
                    long midBits = Double.doubleToLongBits(midVal);
                    long keyBits = Double.doubleToLongBits(key);
                    if (midBits == keyBits) {
                        return mid;
                    }
                    else if (midBits < keyBits) {
                        l = mid + 1;
                    }
                    else {
                        r = mid - 1;
                    }
                }
            }
            return -1;
        }
        //  ------------------ double ------------------/

        //  ------------------ float ------------------>
        public static int binarySearch(float[] a, float key) {
            return binarySearch(a, 0, a.length, key);
        }

        public static int binarySearch(float[] a, int fromIndex, int toIndex, float key) {
            checkRange(a.length, fromIndex, toIndex);
            int l = fromIndex;
            int r = toIndex - 1;

            while (l <= r) {
                int mid = (l + r) / 2;
                float midVal = a[mid];

                if (midVal < key) {
                    l = mid + 1;
                }
                else if (midVal > key) {
                    r = mid - 1;
                }
                else {
                    int midBits = Float.floatToIntBits(midVal);
                    int keyBits = Float.floatToIntBits(key);
                    if (midBits == keyBits) {
                        return mid;
                    }
                    else if (midBits < keyBits) {
                        l = mid + 1;
                    }
                    else {
                        r = mid - 1;
                    }
                }
            }
            return -1;
        }
        //  ------------------ float ------------------/

    }

    public static class Collections {

        private Collections() {}

        public static <T> int binarySearch(List<? extends Comparable<? super T>> list, T key) {
            int l = 0;
            int r = list.size()-1;

            while (l <= r) {
                int mid = (l + r) / 2;
                Comparable<? super T> midVal = list.get(mid);
                int cmp = midVal.compareTo(key);

                if (cmp < 0) {
                    l = mid + 1;
                }
                else if (cmp > 0) {
                    r = mid - 1;
                }
                else {
                    return mid;
                }
            }
            return -1;
        }

        public static <T> int binarySearch(List<? extends T> list, T key, Comparator<T> comparator) {
            if (comparator == null) return binarySearch((List<? extends Comparable<? super T>>) list, key);

            int l = 0;
            int r = list.size()-1;

            while (l <= r) {
                int mid = (l + r) / 2;
                T midVal = list.get(mid);
                int cmp = comparator.compare(midVal, key);

                if (cmp < 0) {
                    l = mid + 1;
                }
                else if (cmp > 0) {
                    r = mid - 1;
                }
                else {
                    return mid;
                }
            }
            return -1;
        }

    }

}
