package cn.he.zhao.bbs.entityUtil.my;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;


/**
 * 描述:
 * CollectionUtils
 *
 * @Author HeFeng
 * @Create 2018-07-24 15:56
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static List<Integer> getRandomIntegers(int start, int end, int size) {
        if (size > end - start + 1) {
            throw new IllegalArgumentException("The specified size more then (end - start + 1)!");
        } else {
            List<Integer> integers = genIntegers(start, end);
            ArrayList ret = new ArrayList();

            while(ret.size() < size) {
                int remainsSize = integers.size();
                int index = (int)(Math.random() * (double)(remainsSize - 1));
                Integer i = (Integer)integers.get(index);
                ret.add(i);
                integers.remove(i);
            }

            return ret;
        }
    }

    public static List<Integer> genIntegers(int start, int end) {
        List<Integer> ret = new ArrayList();

        for(int i = 0; i <= end; ++i) {
            ret.add(i + start);
        }

        return ret;
    }

    public static <T> Set<T> arrayToSet(T[] array) {
        if (null == array) {
            return Collections.emptySet();
        } else {
            Set<T> ret = new HashSet();

            for(int i = 0; i < array.length; ++i) {
                T object = array[i];
                ret.add(object);
            }

            return ret;
        }
    }

    public static <T> JSONArray listToJSONArray(List<T> list) {
        JSONArray ret = new JSONArray();
        if (null == list) {
            return ret;
        } else {
            Iterator var2 = list.iterator();

            while(var2.hasNext()) {
                T object = (T)var2.next();
                ret.put(object);
            }

            return ret;
        }
    }

    public static <T> JSONArray toJSONArray(Collection<T> collection) {
        JSONArray ret = new JSONArray();
        if (null == collection) {
            return ret;
        } else {
            Iterator var2 = collection.iterator();

            while(var2.hasNext()) {
                T object = (T)var2.next();
                ret.put(object);
            }

            return ret;
        }
    }

    public static <T> Set<T> jsonArrayToSet(JSONArray jsonArray) {
        if (null == jsonArray) {
            return Collections.emptySet();
        } else {
            Set<T> ret = new HashSet();

            for(int i = 0; i < jsonArray.length(); ++i) {
                ret.add((T)jsonArray.opt(i));
            }

            return ret;
        }
    }

    public static <T> List<T> jsonArrayToList(JSONArray jsonArray) {
        if (null == jsonArray) {
            return Collections.emptyList();
        } else {
            List<T> ret = new ArrayList();

            for(int i = 0; i < jsonArray.length(); ++i) {
                ret.add((T)jsonArray.opt(i));
            }

            return ret;
        }
    }

    public static <T> T[] jsonArrayToArray(JSONArray jsonArray, Class<? extends T[]> newType) {
        if (null == jsonArray) {
            return (T[])(Object[])(new Object[0]);
        } else {
            int newLength = jsonArray.length();
            Object[] original = new Object[newLength];

            for(int i = 0; i < newLength; ++i) {
                original[i] = jsonArray.opt(i);
            }

            return Arrays.copyOf(original, newLength, newType);
        }
    }
}