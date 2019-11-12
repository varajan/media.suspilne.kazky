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

public class ActivityReaders extends ActivityBase {
    private LinearLayout taleReadersList;
    private ImageView sortIcon;
    private ImageView searchIcon;
    private EditText searchField;
    private TextView nothing;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        filterTaleReaders();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.readers_menu;
        super.onCreate(savedInstanceState);

        taleReadersList = findViewById(R.id.taleReadersList);
        searchField = findViewById(R.id.searchField);
        searchIcon = findViewById(R.id.searchIcon);
        sortIcon = findViewById(R.id.sortIcon);
        nothing = findViewById(R.id.nothingToShow);

        findViewById(R.id.toolbar).setOnClickListener(onSearchClick);
        searchIcon.setOnClickListener(onSearchClick);
        sortIcon.setOnClickListener(onSortClick);
        searchField.setOnEditorActionListener(onSearchKey);
        searchField.setOnTouchListener(onClearIconClick);

        showTaleReaders();
        filterTaleReaders();
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener onClearIconClick = (view, event) -> {
        int actionX = (int) event.getX();
        int viewWidth = view.getWidth();
        int buttonWidth = HSettings.dpToPx(50);

        if (viewWidth - buttonWidth <= actionX){
            searchField.setText("");

            hideSearch();
            filterTaleReaders();
            return true;
        }

        return false;
    };

    private TextView.OnEditorActionListener onSearchKey = (view, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            hideSearch();
            filterTaleReaders();
            return true;
        }
        return false;
    };

    private View.OnClickListener onSearchClick = view -> showSearch();

    private View.OnClickListener onSortClick = view -> {
        HSettings.setBoolean("isAscSorted", !HSettings.getBoolean("isAscSorted"));

        showTaleReaders();
        filterTaleReaders();
    };

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
            filterTaleReaders();
            return false;
        }

        return super.onKeyDown(keycode, event);
    }

    private View.OnClickListener onPlayClick = view -> {
        Intent intent = new Intent(this, ActivityTales.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("filter", view.getTag().toString());
        intent.putExtra("returnToComposers", true);
        startActivityForResult(intent, 0);
    };

    private void showTaleReaders(){
        sortIcon.setImageResource(Readers.isAscSorted() ? R.drawable.ic_sort_alpha : R.drawable.ic_soft_digits);
        taleReadersList.removeViews(1, taleReadersList.getChildCount()-1);
        nothing.setVisibility(View.GONE);

        for (final Reader reader:new Readers().readers) {
            View composerView = LayoutInflater.from(this).inflate(R.layout.tale_item, taleReadersList, false);
            composerView.setTag(reader.getName());
            taleReadersList.addView(composerView);
            reader.setViewDetails(this);

            ImageView tracksButton = composerView.findViewById(R.id.play);
            tracksButton.setVisibility(View.GONE);
            composerView.setOnClickListener(onPlayClick);
        }
    }

    private void filterTaleReaders(){
        boolean anything = false;
        String filter = searchField.getText().toString();
        activityTitle.setText(filter.equals("") ? getString(R.string.taleReaders) : "\u2315 " + filter);

        for (int i = 0; i < taleReadersList.getChildCount(); i++){
            View composer = taleReadersList.getChildAt(i);
            Object tag = composer.getTag();

            if (tag == null) continue;

            boolean visible = tag.toString().toLowerCase().contains(filter.toLowerCase());

            anything = anything || visible;
            composer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        nothing.setVisibility(anything ? View.GONE : View.VISIBLE);
    }
}
