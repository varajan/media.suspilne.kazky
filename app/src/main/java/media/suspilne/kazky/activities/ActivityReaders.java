package media.suspilne.kazky.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.stream.Collectors;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.activities.views.ReaderView;
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

        if (nothingToShow) resetFilter();
        showReaders();
        applyFilter(this::filterReaders);
    }

    private final View.OnClickListener onReaderClick = view -> {
        Intent intent = new Intent(this, ActivityTales.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(Kazky.Constants.returnToReaders, true);

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

        for (final String category : categories) {
            boolean enabled = Tales.getShowCategory(activityName, category);
            Tales.setShowCategory(fromReadersFilter, category, enabled);
        }

        startActivityForResult(intent, 0);
    };

    private int filterReaders(String filter, boolean showOnlyFavorite, List<String> categories) {
        int nothingToShowVisibility = View.VISIBLE;

        for (final Reader reader: new Readers(filter, showOnlyFavorite, categories).Readers) {
            ReaderView readerView = new ReaderView(reader);

            if (reader.talesCount > 0) {
                readerView.show(this, reader.talesCount);
                nothingToShowVisibility = View.GONE;
            } else {
                readerView.hide();
            }
        }

        return nothingToShowVisibility;
    }

    private void showReaders() {
        int nothingToShowElements = 2;
        String filter = Tales.getFilter(activityName);
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite(activityName);
        List<String> selectedCategories = categories.stream()
                .filter(category -> Tales.getShowCategory(activityName, category))
                .collect(Collectors.toList());

        ItemsList.removeViews(nothingToShowElements, ItemsList.getChildCount() - nothingToShowElements);

        for (final Reader reader:new Readers(filter, showOnlyFavorite, selectedCategories).Readers) {
            View readerView = LayoutInflater.from(this).inflate(R.layout.reader_item, ItemsList, false);
            readerView.setTag(reader.name);
            ItemsList.addView(readerView);
            new ReaderView(reader).setViewDetails();
            readerView.setOnClickListener(onReaderClick);
        }
    }
}