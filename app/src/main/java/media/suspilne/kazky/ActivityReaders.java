package media.suspilne.kazky;

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

public class ActivityReaders extends ActivityMain {
    private LinearLayout ReadersList;
    private TextView titleFld;
    private TextView nothing;
    AppCompatButton showAllBtn;
    private List<Integer> allCategories;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        filterReaders();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.readers_menu;
        super.onCreate(savedInstanceState);

        FloatingActionButton searchBtn = findViewById(R.id.searchBtn);
        ReadersList = findViewById(R.id.itemsList);
        titleFld = findViewById(R.id.title);
        nothing = findViewById(R.id.nothingToShow);
        showAllBtn = findViewById(R.id.showAllBtn);
        allCategories = Categories.NameIds;

        searchBtn.setOnClickListener(v -> showFilterDialog(allCategories, this::filterReaders));
        titleFld.setOnClickListener(v -> showFilterDialog(allCategories, this::filterReaders));

        showReaders();
        filterReaders();
    }

    private final View.OnClickListener onReaderClick = view -> {
        Intent intent = new Intent(this, ActivityTales.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("returnToReaders", true);

        Tales.setFilter(view.getTag().toString());
        startActivityForResult(intent, 0);
    };

    private void filterReaders() {
        int nothingToShowVisibility = View.VISIBLE;

        String filter = Tales.getFilter();
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite();
        List<Integer> categories = allCategories.stream()
                .filter(category -> Tales.getShowCategory(this.getResourceString(category)))
                .collect(Collectors.toList());

        for (final Reader reader: new Readers().Readers) {
            if (reader.matchesFilter(filter, showOnlyFavorite, categories)) {
                reader.show();
                nothingToShowVisibility = View.GONE;
            } else {
                reader.hide();
            }
        }

        boolean hideSearchText = !showOnlyFavorite && filter.isEmpty() && categories.equals(allCategories);
        titleFld.setText(hideSearchText ? this.getText(R.string.readers) : getSearchFieldText(allCategories));
        nothing.setVisibility(nothingToShowVisibility);
        showAllBtn.setVisibility(nothingToShowVisibility);
        showAllBtn.setOnClickListener(v -> {
            List<String> categoryNames = allCategories.stream().map(this::getString).toList();
            saveFilters("", false, categoryNames, allCategories);
            filterReaders();
        });
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