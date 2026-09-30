package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.helpers.StringHelper;

public class Readers {
    public List<Reader> Readers;

    public Readers() {
        List<Reader> items = getReaders();

        if (isAscSorted()) {
            items.sort(Comparator.comparing(Reader::getName));
        } else {
            items.sort((c1, c2) -> c2.talesCount.compareTo(c1.talesCount));
        }

        Readers = items;
    }

    public static void setAscSorting(boolean value) {
        SettingsHelper.setBoolean(Kazky.Constants.isAscSorted, value);
    }

    public static boolean isAscSorted() {
        return SettingsHelper.getBoolean(Kazky.Constants.isAscSorted);
    }

    private static List<Reader> getReaders() {
        Activity activity = MainActivity.getActivity();
        List<Reader> readers = new ArrayList<>();
        String postfix = "_description";
        Field[] fields = R.string.class.getFields();

        for (Field field : fields) {
            String fieldName = field.getName();

            if (!fieldName.endsWith(postfix)) {
                continue;
            }

            try {
                int descriptionResId = field.getInt(null);
                String name = StringHelper.substringTo(fieldName, postfix);
                @SuppressLint("DiscouragedApi")
                int nameResId = activity.getResources().getIdentifier(
                        name,
                        "string",
                        activity.getPackageName()
                );

                if (nameResId != 0) {
                    readers.add(new Reader(nameResId, descriptionResId));
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return readers;
    }
}