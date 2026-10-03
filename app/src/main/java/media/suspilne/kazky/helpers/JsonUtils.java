package media.suspilne.kazky.helpers;

import com.google.gson.Gson;
import java.lang.reflect.Type;

public class JsonUtils {
    private static final Gson gson = new Gson();

    public static <T> T fromJson(String json, Type typeOfT) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, typeOfT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}