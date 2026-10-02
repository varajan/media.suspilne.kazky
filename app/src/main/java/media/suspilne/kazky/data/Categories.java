package media.suspilne.kazky.data;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.data.dto.CategoryDto;
import media.suspilne.kazky.helpers.AssetUtils;
import media.suspilne.kazky.helpers.JsonUtils;

public class Categories {
    public static final List<CategoryDto> Items;

    static {
        String jsonString = AssetUtils.loadJSON(MainActivity.getActivity(), "categories.json");
        Type listType = new TypeToken<List<CategoryDto>>() {}.getType();
        Items = JsonUtils.fromJson(jsonString, listType);
    }

    public static List<String> NameIds = Items.stream()
            .map(CategoryDto::title)
            .collect(Collectors.toList());
}
