package media.suspilne.kazky.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import media.suspilne.kazky.data.Categories;
import media.suspilne.kazky.R;
import media.suspilne.kazky.data.Reader;
import media.suspilne.kazky.data.Readers;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.data.Tales;
import media.suspilne.kazky.helpers.StringHelper;

public class ActivityReaders extends ListActivity {
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        applyFilter(this::filterReaders);
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.readers_menu;
        activityName = defaultTitleText = this.getString(R.string.readers);
        categories = Categories.NameIds;
        super.onCreate(savedInstanceState);
        initControls();

        searchBtn.setOnClickListener(v -> showFilterDialog(() -> applyFilter(this::filterReaders)));
        titleFld.setOnClickListener(v -> showFilterDialog(() -> applyFilter(this::filterReaders)));
        boolean nothingToShow = SettingsHelper.getInt(activityName) == View.VISIBLE;

        showReaders();
        if (nothingToShow) resetFilter();
        applyFilter(this::filterReaders);
    }

    private final View.OnClickListener onReaderClick = view -> {
        Intent intent = new Intent(this, ActivityTales.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("returnToReaders", true);

        String readerName = view.getTag().toString();
        TextView readerView = findViewById(R.id.itemsList).findViewWithTag(readerName).findViewById(R.id.description);
        String readerInfo = readerView.getText().toString();
        String talesFilter = Tales.getFilter(activityName);
        String readerFilter = StringHelper.containsIgnoreCase(readerName, talesFilter) || StringHelper.containsIgnoreCase(readerInfo, talesFilter)
                ? ""
                : talesFilter;

        String filter = readerName + ", " + readerFilter;
        filter = StringHelper.trim(filter, ",");

        Tales.setFilter(fromReadersFilter, filter);
        Tales.setShowOnlyFavorite(fromReadersFilter,Tales.getShowOnlyFavorite(activityName));

        for (final Integer categoryId : categories) {
            String category = getResourceString(categoryId);
            boolean enabled = Tales.getShowCategory(activityName, category);
            Tales.setShowCategory(fromReadersFilter, category, enabled);
        }

        startActivityForResult(intent, 0);
    };

    private int filterReaders(String filter, boolean showOnlyFavorite, List<Integer> categories) {
        int nothingToShowVisibility = View.VISIBLE;

        for (final Reader reader: new Readers().Readers) {
            Integer matchedTales = reader.getMatchedTales(filter, showOnlyFavorite, categories);

            if (matchedTales > 0) {
                reader.show(this, matchedTales);
                nothingToShowVisibility = View.GONE;
            } else {
                reader.hide();
            }
        }

        return nothingToShowVisibility;
    }

    private void showReaders() {
        int nothingToShowElements = 2;
        ItemsList.removeViews(nothingToShowElements, ItemsList.getChildCount() - nothingToShowElements);

        for (final Reader reader:new Readers().Readers) {
            View readerView = LayoutInflater.from(this).inflate(R.layout.reader_item, ItemsList, false);
            readerView.setTag(reader.getName());
            ItemsList.addView(readerView);
            reader.setViewDetails();
            readerView.setOnClickListener(onReaderClick);
        }
    }
}