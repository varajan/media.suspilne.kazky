package media.suspilne.kazky.activities;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import media.suspilne.kazky.R;
import media.suspilne.kazky.data.Tales;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.helpers.StringHelper;

public abstract class ListActivity extends MainActivity {
    protected String activityName;
    protected String defaultTitleText;
    protected String fromReadersFilter = "fromReadersFilter";
    protected List<String> categories;

    protected FloatingActionButton searchBtn;
    protected LinearLayout ItemsList;
    protected TextView titleFld;
    protected TextView nothing;
    protected AppCompatButton showAllBtn;
    private List<String> showOnlyFavorite;

    protected void initControls() {
        searchBtn = findViewById(R.id.searchBtn);
        ItemsList = findViewById(R.id.itemsList);
        titleFld = findViewById(R.id.title);
        nothing = findViewById(R.id.nothingToShow);
        showAllBtn = findViewById(R.id.showAllBtn);
        showOnlyFavorite = Collections.singletonList(this.getString(R.string.showOnlyFavorite));
    }

    protected void showFilterDialog(Runnable filterAction) {
        View dialogView = getLayoutInflater().inflate(R.layout.filter_dialog, null);
        EditText searchField = dialogView.findViewById(R.id.searchField);
        LinearLayout showOnlyFavoriteCheckbox = dialogView.findViewById(R.id.favoritesGroup);
        LinearLayout categoriesCheckboxes = dialogView.findViewById(R.id.categoriesGroup);

        List<String> onlyFavorite = Tales.getShowOnlyFavorite(activityName)
                ? Collections.singletonList(this.getString(R.string.showOnlyFavorite))
                : Collections.emptyList();
        List<String> checkedCategories = categories.stream()
                .filter(c -> Tales.getShowCategory(activityName, c))
                .collect(Collectors.toList());

        String initialSearchText = Tales.getFilter(activityName);
        searchField.setText(initialSearchText);
        setCheckboxes(showOnlyFavoriteCheckbox, showOnlyFavorite, onlyFavorite);
        setCheckboxes(categoriesCheckboxes, categories, checkedCategories);

        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.filtersDialog)
                .setView(dialogView)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    String searchText = searchField.getText().toString();
                    boolean showOnlyFavorites = !getSelectedCheckboxes(showOnlyFavoriteCheckbox).isEmpty();
                    List<String> selectedCategories = getSelectedCheckboxes(categoriesCheckboxes);

                    saveFilters(searchText, showOnlyFavorites, selectedCategories, categories);
                    filterAction.run();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    protected void resetFilter() { resetFilter(null); }

    protected void resetFilter(Runnable filterAction) {
        List<String> categoryNames = new ArrayList<>(categories);
        saveFilters("", false, categoryNames, categories);
        if (filterAction != null) filterAction.run();
    }

    protected void applyFilter(FilterAction filterAction) {
        String filter = Tales.getFilter(activityName);
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite(activityName);
        List<String> selectedCategories = categories.stream()
                .filter(category -> Tales.getShowCategory(activityName, category))
                .collect(Collectors.toList());

        int nothingToShowVisibility = filterAction.run(filter, showOnlyFavorite, selectedCategories);

        setSearchFieldText();
        nothing.setVisibility(nothingToShowVisibility);
        showAllBtn.setVisibility(nothingToShowVisibility);
        showAllBtn.setOnClickListener(v -> resetFilter(() -> this.applyFilter(filterAction)));

        SettingsHelper.setInt(activityName, nothingToShowVisibility);
    }

    protected void setSearchFieldText() {
        String filter = Tales.getFilter(activityName);
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite(activityName);
        List<String> selectedCategories = categories.stream()
                .filter(category -> Tales.getShowCategory(activityName, category))
                .collect(Collectors.toList());

        boolean hideSearchText = !showOnlyFavorite && filter.isEmpty() && selectedCategories.equals(categories);
        titleFld.setText(hideSearchText ? defaultTitleText : getSearchFieldText(categories));
    }

    private String getSearchFieldText(List<String> allCategoryIds) {
        String filter = Tales.getFilter(activityName);
        String searchFieldText = "";
        List<String> categories = allCategoryIds.stream()
                .filter(category -> Tales.getShowCategory(activityName, category))
                .collect(Collectors.toList());
        boolean allCategoriesSelected = categories.equals(allCategoryIds);
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite(activityName);

        searchFieldText += filter;
        if (showOnlyFavorite) { searchFieldText += ", " + this.getString(R.string.favoriteTales); }
        if (!allCategoriesSelected) searchFieldText += ", " + String.join(", ", categories);
        searchFieldText = StringHelper.trim(searchFieldText, ",");

        return "⌕ " + searchFieldText;
    }

    private void setCheckboxes(LinearLayout container, List<String> items, List<String> selected) {
        container.removeAllViews();
        for (@StringRes String item : items) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(item);

            if (selected.contains(item)) checkBox.setChecked(true);
            container.addView(checkBox);
        }
    }

    private List<String> getSelectedCheckboxes(LinearLayout container) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);

            if (view instanceof CheckBox) {
                CheckBox cb = (CheckBox) view;
                if (cb.isChecked()) {
                    result.add(cb.getText().toString());
                }
            }
        }
        return result;
    }

    private void saveFilters(String filter, boolean showOnlyFavorites, List<String> selectedCategories, List<String> categories) {
        Tales.setFilter(activityName, filter);
        Tales.setShowOnlyFavorite(activityName, showOnlyFavorites);

        for (final String category : categories) {
            boolean categoryEnabled = selectedCategories.contains(category);
            Tales.setShowCategory(activityName, category, categoryEnabled);
        }
    }

    @FunctionalInterface
    public interface FilterAction {
        int run(String filter, boolean showOnlyFavorite, List<String> categories);
    }
}
