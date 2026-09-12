package media.suspilne.kazky.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.stream.Collectors;

import media.suspilne.kazky.data.Categories;
import media.suspilne.kazky.R;
import media.suspilne.kazky.data.Reader;
import media.suspilne.kazky.data.Readers;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.data.Tales;

public class ActivityReaders extends ListActivity implements IListActivity {
    private LinearLayout ReadersList;
    private TextView nothing;
    AppCompatButton showAllBtn;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        filterReaders();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.readers_menu;
        activityName = defaultTitleText = this.getString(R.string.readers);
        super.onCreate(savedInstanceState);

        FloatingActionButton searchBtn = findViewById(R.id.searchBtn);
        ReadersList = findViewById(R.id.itemsList);
        titleFld = findViewById(R.id.title);
        nothing = findViewById(R.id.nothingToShow);
        showAllBtn = findViewById(R.id.showAllBtn);
        categories = Categories.NameIds;

        searchBtn.setOnClickListener(v -> showFilterDialog(this::filterReaders));
        titleFld.setOnClickListener(v -> showFilterDialog(this::filterReaders));
        boolean nothingToShow = SettingsHelper.getInt(activityName) == View.VISIBLE;

        showReaders();
        if (nothingToShow) resetFilter();
        filterReaders();
    }

    private final View.OnClickListener onReaderClick = view -> {
        Intent intent = new Intent(this, ActivityTales.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("returnToReaders", true);

        Tales.setFilter(fromReadersFilter, view.getTag().toString());
        Tales.setShowOnlyFavorite(fromReadersFilter,Tales.getShowOnlyFavorite(activityName));

        for (final Integer categoryId : categories) {
            String category = getResourceString(categoryId);
            boolean enabled = Tales.getShowCategory(activityName, category);
            Tales.setShowCategory(fromReadersFilter, category, enabled);
        }

        startActivityForResult(intent, 0);
    };

    private void filterReaders() {
        int nothingToShowVisibility = View.VISIBLE;

        String filter = Tales.getFilter(activityName);
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite(activityName);
        List<Integer> selectedCategories = categories.stream()
                .filter(category -> Tales.getShowCategory(activityName, this.getResourceString(category)))
                .collect(Collectors.toList());

        for (final Reader reader: new Readers().Readers) {
            if (reader.matchesFilter(filter, showOnlyFavorite, selectedCategories)) {
                reader.show();
                nothingToShowVisibility = View.GONE;
            } else {
                reader.hide();
            }
        }

        setSearchFieldText();
        nothing.setVisibility(nothingToShowVisibility);
        showAllBtn.setVisibility(nothingToShowVisibility);
        showAllBtn.setOnClickListener(v -> resetFilter(this::filterReaders));

        SettingsHelper.setInt(activityName, nothingToShowVisibility);
    }

    private void showReaders() {
        int nothingToShowElements = 2;
        ReadersList.removeViews(nothingToShowElements, ReadersList.getChildCount() - nothingToShowElements);

        for (final Reader reader:new Readers().Readers) {
            View readerView = LayoutInflater.from(this).inflate(R.layout.reader_item, ReadersList, false);
            readerView.setTag(reader.getName());
            ReadersList.addView(readerView);
            reader.setViewDetails(this);
            readerView.setOnClickListener(onReaderClick);
        }
    }
}