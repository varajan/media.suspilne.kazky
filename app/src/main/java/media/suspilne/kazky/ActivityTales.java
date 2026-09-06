package media.suspilne.kazky;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityTales extends ActivityMain {
    private Tales tales;
    private LinearLayout TalesList;
    private TextView titleFld;
    private boolean returnToReaders = false;
    private String searchText = "";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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
            super.resetQuitTimeout();
            this.resetVolumeReduceTimer();
        }
    }

    private void showTales() {
        boolean showBigImages = SettingsHelper.getBoolean("showBigImages");

        for (final Tale tale:tales.getTalesList()) {
            View taleView = LayoutInflater.from(this).inflate(showBigImages ? R.layout.tale_item : R.layout.tale_item_small, TalesList, false);
            taleView.setTag(tale.id);
            TalesList.addView(taleView);
            tale.setViewDetails();

            final ImageView playBtn = taleView.findViewById(R.id.play);
            playBtn.setTag(R.mipmap.tale_play);
            playBtn.setOnClickListener(v -> {
                if (playBtn.getTag().equals(R.mipmap.tale_pause)){
                    Tales.setLastPlaying(tale.id);
                    Tales.setNowPlaying(-1);

                    super.stopPlayerService();
                    playBtn.setImageResource(R.mipmap.tale_play);
                    playBtn.setTag(R.mipmap.tale_play);
                    super.resetVolumeReduceTimer();
                }else{
                    if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                        Toast.makeText(getActivity(), R.string.no_post_notifications_permissions, Toast.LENGTH_LONG).show();
                    }

                    playTale(tale);
                    super.resetQuitTimeout();
                    super.resetVolumeReduceTimer();

                    playBtn.setImageResource(R.mipmap.tale_pause);
                    playBtn.setTag(R.mipmap.tale_pause);
                }
            });

            taleView.findViewById(R.id.favorite).setOnClickListener(v -> {
                tale.resetFavorite();
                Toast.makeText(getActivity(),
                        tale.isFavorite ? getString(R.string.addedToFavorites, tale.getTitle()) : getString(R.string.removedFromFavorites, tale.getTitle()),
                        Toast.LENGTH_LONG).show();
                filterTales();
            });
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
        titleFld = findViewById(R.id.title);
        FloatingActionButton categoriesFilterBtn = findViewById(R.id.categoriesFilterBtn);
        ImageButton searchIcon = findViewById(R.id.searchIcon);

        Intent intent = getIntent();
        returnToReaders = intent.getBooleanExtra("returnToReaders", false);
        TalesList = findViewById(R.id.talesList);
        tales = new Tales();

        if (returnToReaders) {
            Tales.setShowOnlyFavorite(false);

            for (final Integer categoryId : Categories.NameIds) {
                String category = getResourceString(categoryId);
                Tales.setShowCategory(category, true);
            }
        }
        searchText = Tales.getFilter();

        categoriesFilterBtn.setOnClickListener(v -> showFilterDialog());
        searchIcon.setOnClickListener(v -> showFilterDialog());
        titleFld.setOnClickListener(v -> showFilterDialog());

        showTales();
        filterTales();
        continueTale(savedInstanceState);
        continueDownloadTales();
        suggestToDownloadFavoriteTales();
        registerReceiver();
    }

    private final List<Integer> showOnlyFavorite = Collections.singletonList(R.string.showOnlyFavorite);

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.filter_dialog, null);
        EditText searchField = dialogView.findViewById(R.id.searchField);
        LinearLayout showOnlyFavoriteContainer = dialogView.findViewById(R.id.favoritesGroup);
        LinearLayout categoriesContainer = dialogView.findViewById(R.id.categoriesGroup);

        List<String> onlyFavorite = Tales.getShowOnlyFavorite()
                ? Collections.singletonList(this.getResourceString(R.string.showOnlyFavorite))
                : Collections.emptyList();
        List<String> checkedCategories = Categories.NameIds.stream()
                .map(this::getResourceString)
                .filter(Tales::getShowCategory)
                .collect(Collectors.toList());

        searchField.setText(searchText);
        populateContainer(showOnlyFavoriteContainer, showOnlyFavorite, onlyFavorite);
        populateContainer(categoriesContainer, Categories.NameIds, checkedCategories);

        new AlertDialog.Builder(this)
                .setTitle(R.string.filtersDialog)
                .setView(dialogView)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    searchText = searchField.getText().toString();
                    List<String> showOnlyFavorites = getSelectedItems(showOnlyFavoriteContainer);
                    List<String> selectedCategories = getSelectedItems(categoriesContainer);

                    saveFilters(searchText, !showOnlyFavorites.isEmpty(), selectedCategories);
                    filterTales();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void populateContainer(LinearLayout container, List<Integer> items, List<String> selected) {
        container.removeAllViews();
        for (@StringRes int item : items) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(item);

            String itemText = this.getResourceString(item);
            if (selected.contains(itemText)) checkBox.setChecked(true);
            container.addView(checkBox);
        }
    }

    private List<String> getSelectedItems(LinearLayout container) {
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

    private void saveFilters(String filter, boolean showOnlyFavorites, List<String> selectedCategories) {
        Tales.setFilter(filter);
        Tales.setShowOnlyFavorite(showOnlyFavorites);

        for (final Integer categoryId : Categories.NameIds) {
            String category = getResourceString(categoryId);
            boolean categoryEnabled = selectedCategories.contains(category);
            Tales.setShowCategory(category, categoryEnabled);
        }
    }

    private void filterTales() {
        View nothing = findViewById(R.id.nothingToShow);
        String searchFieldText = "";
        int visibility = View.VISIBLE;
        StringBuilder list = new StringBuilder();

        String filter = Tales.getFilter();
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite();
        List<Integer> categories = Categories.NameIds.stream()
                .filter(category -> Tales.getShowCategory(this.getResourceString(category)))
                .collect(Collectors.toList());

        boolean allCategoriesSelected = categories.equals(Categories.NameIds);
        boolean hideSearchText = !showOnlyFavorite && filter.isEmpty() && allCategoriesSelected;

        searchFieldText += filter;
        if (showOnlyFavorite) { searchFieldText += ", " + this.getString(R.string.favoriteTales); }
        if (!allCategoriesSelected) searchFieldText += ", " + categories.stream()
                .map(this::getString)
                .collect(Collectors.joining(", "));
        searchFieldText = searchFieldText.replaceAll("^,+|,+$", "").trim();

        for (final Tale tale:tales.getTalesList()) {
            if (tale.shouldBeShown(showOnlyFavorite, categories, filter)){
                tale.show();
                visibility = View.GONE;
                list.append(tale.id).append(";");
            } else {
                tale.hide();
            }
        }

        titleFld.setText(hideSearchText ? this.getText(R.string.allTales) : searchFieldText);
        nothing.setVisibility(visibility);
        SettingsHelper.setString("filteredTalesList", list.toString());
    }

    private void playTale(Tale tale){
        super.stopPlayerService();

        if (tale.id != -1){
            Intent stream = new Intent(this, PlayerService.class);
            stream.putExtra("tale.id", tale.id);
            stream.putExtra("type", getString(R.string.tales));
            startForegroundService(stream);
        }

        setPlayBtnIcon(false);
    }

    private void setPlayBtnIcon() { setPlayBtnIcon(false); }

    private void setPlayBtnIcon(boolean scrollToTale){
        LinearLayout list = findViewById(R.id.talesList);
        Tale currentTale = tales.getById(Tales.getNowPlaying());
        boolean isPaused = Tales.isPaused();

        for (Tale tale:tales.getTalesList()){
            ImageView btn = list.findViewWithTag(tale.id).findViewById(R.id.play);
            boolean isPlaying = !isPaused && currentTale != null && tale.id == currentTale.id;

            btn.setImageResource(isPlaying ? R.mipmap.tale_pause : R.mipmap.tale_play);
            btn.setTag(isPlaying ? R.mipmap.tale_pause : R.mipmap.tale_play);
        }

        if (scrollToTale && currentTale != null){
            currentTale.scrollIntoView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
        setPlayBtnIcon();
    }

    private void registerReceiver() {
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(SettingsHelper.application);
            this.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        }catch (Exception e){
            // nothing
        }
    }

    private void unregisterReceiver() {
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
                Tales.setPause(true);
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