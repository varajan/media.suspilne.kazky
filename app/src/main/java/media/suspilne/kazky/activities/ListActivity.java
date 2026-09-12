package media.suspilne.kazky.activities;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import media.suspilne.kazky.R;
import media.suspilne.kazky.data.Tales;

public class ListActivity extends MainActivity {
    protected String activityName;
    protected String fromReadersFilter = "fromReadersFilter";
    protected List<Integer> categories;

    protected final List<Integer> showOnlyFavorite = Collections.singletonList(R.string.showOnlyFavorite);

    protected void showFilterDialog(Runnable filter) {
        View dialogView = getLayoutInflater().inflate(R.layout.filter_dialog, null);
        EditText searchField = dialogView.findViewById(R.id.searchField);
        LinearLayout showOnlyFavoriteContainer = dialogView.findViewById(R.id.favoritesGroup);
        LinearLayout categoriesContainer = dialogView.findViewById(R.id.categoriesGroup);

        List<String> onlyFavorite = Tales.getShowOnlyFavorite(activityName)
                ? Collections.singletonList(this.getResourceString(R.string.showOnlyFavorite))
                : Collections.emptyList();
        List<String> checkedCategories = categories.stream()
                .map(this::getResourceString)
                .filter(c -> Tales.getShowCategory(activityName, c))
                .collect(Collectors.toList());

        String initialSearchText = Tales.getFilter(activityName);
        searchField.setText(initialSearchText);
        populateContainer(showOnlyFavoriteContainer, showOnlyFavorite, onlyFavorite);
        populateContainer(categoriesContainer, categories, checkedCategories);

        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.filtersDialog)
                .setView(dialogView)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    String searchText = searchField.getText().toString();
                    List<String> showOnlyFavorites = getSelectedItems(showOnlyFavoriteContainer);
                    List<String> selectedCategories = getSelectedItems(categoriesContainer);

                    saveFilters(searchText, !showOnlyFavorites.isEmpty(), selectedCategories, categories);
                    filter.run();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    protected void resetFilter() { resetFilter(null); }

    protected void resetFilter(Runnable filter) {
        List<String> categoryNames = categories.stream().map(this::getString).toList();
        saveFilters("", false, categoryNames, categories);
        if (filter != null) filter.run();
    }

    protected void populateContainer(LinearLayout container, List<Integer> items, List<String> selected) {
        container.removeAllViews();
        for (@StringRes int item : items) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(item);

            String itemText = this.getResourceString(item);
            if (selected.contains(itemText)) checkBox.setChecked(true);
            container.addView(checkBox);
        }
    }

    protected List<String> getSelectedItems(LinearLayout container) {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox cb = (CheckBox) view;
                if (cb.isChecked()) {
                    selected.add(cb.getText().toString());
                }
            }
        }
        return selected;
    }

    protected void saveFilters(String filter, boolean showOnlyFavorites, List<String> selectedCategories, List<Integer> categories) {
        Tales.setFilter(activityName, filter);
        Tales.setShowOnlyFavorite(activityName, showOnlyFavorites);

        for (final Integer categoryId : categories) {
            String category = getResourceString(categoryId);
            boolean categoryEnabled = selectedCategories.contains(category);
            Tales.setShowCategory(activityName, category, categoryEnabled);
        }
    }

    protected String getSearchFieldText(List<Integer> allCategoryIds) {
        String filter = Tales.getFilter(activityName);
        String searchFieldText = "";
        List<Integer> categories = allCategoryIds.stream()
                .filter(category -> Tales.getShowCategory(activityName, this.getResourceString(category)))
                .collect(Collectors.toList());
        boolean allCategoriesSelected = categories.equals(allCategoryIds);
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite(activityName);

        searchFieldText += filter;
        if (showOnlyFavorite) { searchFieldText += ", " + this.getString(R.string.favoriteTales); }
        if (!allCategoriesSelected) searchFieldText += ", " + categories.stream()
                .map(this::getString)
                .collect(Collectors.joining(", "));
        searchFieldText = searchFieldText
                .trim()
                .replaceAll("^,+|,+$", "")
                .trim();

        return "⌕ " + searchFieldText;
    }
}

interface IListActivity { }