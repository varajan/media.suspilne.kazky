package media.suspilne.kazky;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActivityTales extends ActivityMain {
    private Tales Tales;
    private ImageView favoriteIcon;
    private ImageView searchIcon;
    private EditText searchField;
    private LinearLayout TalesList;
    private boolean returnToReaders = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        unregisterReceiver();

        outState.putBoolean("returnToReaders", returnToReaders);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        registerReceiver();
        continueTale(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        setPlayBtnIcon(true);
    }

    private void continueTale(Bundle bundle){
        if (bundle == null) return;

        returnToReaders = bundle.getBoolean("returnToReaders");

        if (Tales.getNowPlaying() > 0){
            setPlayBtnIcon();
            super.setQuiteTimeout();
        }
    }

    private void hideSearch(){
        searchIcon.setVisibility(View.VISIBLE);
        favoriteIcon.setVisibility(View.VISIBLE);
        searchField.setVisibility(View.GONE);

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(searchField.getWindowToken(), 0);
    }

    private View.OnClickListener search = v -> {
        searchIcon.setVisibility(View.GONE);
        favoriteIcon.setVisibility(View.GONE);
        searchField.setVisibility(View.VISIBLE);
        searchField.requestFocus();

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    };

    @SuppressLint("ClickableViewAccessibility")
    private void addSearchField() {
        favoriteIcon = findViewById(R.id.showFavorite);
        searchIcon = findViewById(R.id.searchIcon);
        searchField = findViewById(R.id.searchField);

        findViewById(R.id.toolbar).setOnClickListener(search);
        searchIcon.setOnClickListener(search);

        favoriteIcon.setOnClickListener(v -> {
            Tales.showOnlyFavorite = !Tales.showOnlyFavorite;
            SettingsHelper.setBoolean("showOnlyFavorite", Tales.showOnlyFavorite);

            filterTales();
        });

        searchField.setText(Tales.filter);
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Tales.filter = v.getText().toString();
                returnToReaders = false;

                hideSearch();
                filterTales();
                return true;
            }
            return false;
        });

        searchField.setOnTouchListener((view, event) -> {
            int actionX = (int) event.getX();
            int viewWidth = view.getWidth();
            int buttonWidth = SettingsHelper.dpToPx(50);

            if (viewWidth - buttonWidth <= actionX){
                searchField.setText("");
                Tales.filter = "";
                returnToReaders = false;

                hideSearch();
                filterTales();
                return true;
            }

            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if (searchField.getVisibility() == View.VISIBLE && (event.getAction() == KeyEvent.ACTION_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_BACK)){
            Tales.filter = searchField.getText().toString();
            hideSearch();
            filterTales();
            return false;
        }

        return super.onKeyDown(keycode, event);
    }

    private void filterTales(){
        SettingsHelper.setString("TalesFilter", Tales.filter);
        favoriteIcon.setImageResource(Tales.showOnlyFavorite ? R.drawable.ic_favorite : R.drawable.ic_all);
        activityTitle.setText(Tales.filter.equals("") ? getString(R.string.tales) : "\u2315 " + Tales.filter);
        View nothing = findViewById(R.id.nothingToShow);
        int visibility = View.VISIBLE;

        for (final Tale tale:Tales.getTales()) {
            if (Tales.showOnlyFavorite && !tale.isFavorite || !tale.matchesFilter(Tales.filter)){
                tale.hide();
            }else{
                tale.show();
                visibility = View.GONE;
            }
        }

        nothing.setVisibility(visibility);
    }

    private void showTales(){
        for (final Tale tale:Tales.getTales()) {
            View TaleView = LayoutInflater.from(this).inflate(R.layout.tale_item, TalesList, false);
            TaleView.setTag(tale.id);
            TalesList.addView(TaleView);
            tale.setViewDetails();

            final ImageView playBtn = TaleView.findViewById(R.id.play);
            playBtn.setTag(R.mipmap.tale_play);
            playBtn.setOnClickListener(v -> {
                if (playBtn.getTag().equals(R.mipmap.tale_pause)){
                    Tales.setLastPlaying(tale.id);
                    Tales.setNowPlaying(-1);

                    super.stopPlayerService();
                    playBtn.setImageResource(R.mipmap.tale_play);
                    playBtn.setTag(R.mipmap.tale_play);
                }else{
                    playTale(tale);
                    setQuiteTimeout();

                    playBtn.setImageResource(R.mipmap.tale_pause);
                    playBtn.setTag(R.mipmap.tale_pause);
                }
            });

            TaleView.findViewById(R.id.favorite).setOnClickListener(v -> { tale.resetFavorite(); filterTales(); });
        }

        setPlayBtnIcon();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (returnToReaders && !drawer.isDrawerOpen(GravityCompat.START)) {
            finish();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.tales_menu;
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String filter = intent.getStringExtra("filter");
        returnToReaders = intent.getBooleanExtra("returnToReaders", false);

        TalesList = findViewById(R.id.talesList);
        Tales = new Tales();
        Tales.filter = filter == null ? "" : filter;

        addSearchField();
        showTales();
        filterTales();
        continueTale(savedInstanceState);
        continueDownloadTales();
        suggestToDownloadFavoriteTales();
        registerReceiver();
    }

    private void playTale(Tale Tale){
        super.stopPlayerService();

        if (Tale.id != -1){
            Intent stream = new Intent(this, PlayerService.class);
            stream.putExtra("Tale.id", Tale.id);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(stream);
            } else {
                startService(stream);
            }
        }

        setPlayBtnIcon(false);
    }

    private void setPlayBtnIcon(){ setPlayBtnIcon(true); }

    private void setPlayBtnIcon(boolean scrollToTale){
        LinearLayout list = findViewById(R.id.talesList);
        Tale Tale = Tales.getById(Tales.getNowPlaying());
        boolean isPaused = Tales.isPaused();

        for (Tale tale:Tales.getTales()){
            ImageView btn = list.findViewWithTag(tale.id).findViewById(R.id.play);
            boolean isPlaying = !isPaused && Tale != null && tale.id == Tale.id;

            btn.setImageResource(isPlaying ? R.mipmap.tale_pause : R.mipmap.tale_play);
            btn.setTag(isPlaying ? R.mipmap.tale_pause : R.mipmap.tale_play);
        }

        if (scrollToTale && Tale != null){
            Tale.scrollIntoView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
        setPlayBtnIcon();
    }

    private void registerReceiver(){
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(SettingsHelper.application);
            this.registerReceiver(receiver, filter);
        }catch (Exception e){
            // nothing
        }
    }

    private void unregisterReceiver(){
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e){ /*nothing*/ }
    }

    @Override
    public void onDestroy() {
       unregisterReceiver();
        super.onDestroy();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        switch (intent.getStringExtra("code")){
            case "SourceIsNotAccessible":
                setPlayBtnIcon();
                Toast.makeText(ActivityTales.this, R.string.no_internet, Toast.LENGTH_LONG).show();
                break;

            case "SetPlayBtnIcon":
                setPlayBtnIcon();
                break;
            }
        }
    };
}