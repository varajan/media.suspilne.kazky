package media.suspilne.kazky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityReaders extends ActivityMain {
    private LinearLayout ReadersList;
    private ImageView sortIcon;
    private ImageView searchIcon;
    private EditText searchField;
    private TextView nothing;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        filterReaders();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.readers_menu;
        super.onCreate(savedInstanceState);

        ReadersList = findViewById(R.id.readersList);
        searchField = findViewById(R.id.searchField);
        searchIcon = findViewById(R.id.searchIcon);
        sortIcon = findViewById(R.id.sortIcon);
        nothing = findViewById(R.id.nothingToShow);

        findViewById(R.id.toolbar).setOnClickListener(onSearchClick);
        searchIcon.setOnClickListener(onSearchClick);
        sortIcon.setOnClickListener(onSortClick);
        searchField.setOnEditorActionListener(onSearchKey);
        searchField.setOnTouchListener(onClearIconClick);

        showReaders();
        filterReaders();
    }

    private final TextView.OnEditorActionListener onSearchKey = (view, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            hideSearch();
            filterReaders();
            return true;
        }
        return false;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onClearIconClick = (view, event) -> {
        int actionX = (int) event.getX();
        int viewWidth = view.getWidth();
        int buttonWidth = SettingsHelper.dpToPx(50);

        if (viewWidth - buttonWidth <= actionX){
            searchField.setText("");

            hideSearch();
            filterReaders();
            return true;
        }

        return false;
    };

    private final View.OnClickListener onPlayClick = view -> {
        Intent intent = new Intent(this, ActivityTales.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("returnToReaders", true);

        Tales.setFilter(view.getTag().toString());
        startActivityForResult(intent, 0);
    };

    private final View.OnClickListener onSortClick = view -> {
        SettingsHelper.setBoolean("isAscSorted", !SettingsHelper.getBoolean("isAscSorted"));

        showReaders();
        filterReaders();
    };

    private final View.OnClickListener onSearchClick = view -> showSearch();

    private void showSearch(){
        searchIcon.setVisibility(View.GONE);
        sortIcon.setVisibility(View.GONE);
        searchField.setVisibility(View.VISIBLE);
        searchField.requestFocus();

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideSearch(){
        searchIcon.setVisibility(View.VISIBLE);
        sortIcon.setVisibility(View.VISIBLE);
        searchField.setVisibility(View.GONE);

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(searchField.getWindowToken(), 0);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if (searchField.getVisibility() == View.VISIBLE && (event.getAction() == KeyEvent.ACTION_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_BACK)){
            hideSearch();
            filterReaders();
            return false;
        }

        return super.onKeyDown(keycode, event);
    }

    private void filterReaders(){
        String filter = searchField.getText().toString();
        View nothing = findViewById(R.id.nothingToShow);
        int visibility = View.VISIBLE;

        activityTitle.setText(filter.equals("") ? getString(R.string.readers) : "\u2315 " + filter);

        for (final Reader reader: new Readers().Readers) {
            if (reader.talesCount > 0 && reader.matchesFilter(filter)){
                reader.show();
                visibility = View.GONE;
            }else{
                reader.hide();
            }
        }

        nothing.setVisibility(visibility);
    }

    private void showReaders(){
        sortIcon.setImageResource(Readers.isAscSorted() ? R.drawable.ic_sort_alpha : R.drawable.ic_soft_digits);
        ReadersList.removeViews(1, ReadersList.getChildCount()-1);
        nothing.setVisibility(View.GONE);

        for (final Reader reader:new Readers().Readers) {
            View readerView = LayoutInflater.from(this).inflate(R.layout.reader_item, ReadersList, false);
            readerView.setTag(reader.getName());
            ReadersList.addView(readerView);
            reader.setViewDetails(this);
            readerView.setOnClickListener(onPlayClick);
        }
    }
}