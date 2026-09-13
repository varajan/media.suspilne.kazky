package media.suspilne.kazky.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import media.suspilne.kazky.activities.views.TaleView;
import media.suspilne.kazky.data.Categories;
import media.suspilne.kazky.player.PlayerService;
import media.suspilne.kazky.R;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.data.Tale;
import media.suspilne.kazky.data.Tales;

public class ActivityTales extends ListActivity {
    private Tales tales;
    private boolean returnToReaders = false;

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

    private void continueTale(Bundle bundle) {
        if (bundle == null) return;

        returnToReaders = bundle.getBoolean("returnToReaders");

        if (Tales.getNowPlaying() > 0) {
            setPlayBtnIcon();
            super.resetQuitTimeout();
            this.resetVolumeReduceTimer();
        }
    }

    private void showTales() {
        boolean showBigImages = SettingsHelper.getBoolean("showBigImages");

        for (final Tale tale:tales.getTalesList()) {
            View taleView = LayoutInflater.from(this).inflate(showBigImages ? R.layout.tale_item : R.layout.tale_item_small, ItemsList, false);
            taleView.setTag(tale.id);
            ItemsList.addView(taleView);
            new TaleView(tale).setViewDetails();

            final ImageView playBtn = taleView.findViewById(R.id.play);
            playBtn.setTag(R.mipmap.tale_play);
            playBtn.setOnClickListener(v -> {
                if (playBtn.getTag().equals(R.mipmap.tale_pause)) {
                    Tales.setLastPlaying(tale.id);
                    Tales.setNowPlaying(-1);

                    super.stopPlayerService();
                    playBtn.setImageResource(R.mipmap.tale_play);
                    playBtn.setTag(R.mipmap.tale_play);
                    super.resetVolumeReduceTimer();
                } else {
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
                new TaleView(tale).setFavoriteIcon();
                applyFilter(this::filterTales);
            });
        }

        setPlayBtnIcon();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (returnToReaders && !drawer.isDrawerOpen(GravityCompat.START)) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.tales_menu;
        defaultTitleText = this.getString(R.string.allTales);

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        returnToReaders = intent.getBooleanExtra("returnToReaders", false);
        initControls();
        tales = new Tales();
        categories = Categories.NameIds;

        activityName = returnToReaders ? fromReadersFilter : this.getString(R.string.tales);
        boolean nothingToShow = ! activityName.equals(fromReadersFilter) && SettingsHelper.getInt(activityName) == View.VISIBLE;

        searchBtn.setOnClickListener(v -> showFilterDialog(() -> applyFilter(this::filterTales)));
        titleFld.setOnClickListener(v -> showFilterDialog(() -> applyFilter(this::filterTales)));

        showTales();
        if (nothingToShow) resetFilter();

        applyFilter(this::filterTales);
        continueTale(savedInstanceState);
        continueDownloadTales();
        suggestToDownloadFavoriteTales();
        registerReceiver();
    }

    private int filterTales(String filter, boolean showOnlyFavorite, List<Integer> categories) {
        int nothingToShowVisibility = View.VISIBLE;
        StringBuilder list = new StringBuilder();

        for (final Tale tale:tales.getTalesList()) {
            TaleView taleView = new TaleView(tale);

            if (tale.shouldBeShown(showOnlyFavorite, categories, filter)) {
                taleView.show();
                nothingToShowVisibility = View.GONE;
                list.append(tale.id).append(";");
            } else {
                taleView.hide();
            }
        }

        SettingsHelper.setString("filteredTalesList", list.toString().trim());

        return nothingToShowVisibility;
    }

    private void playTale(Tale tale) {
        super.stopPlayerService();

        if (tale.id != -1) {
            Intent stream = new Intent(this, PlayerService.class);
            stream.putExtra("tale.id", tale.id);
            stream.putExtra("type", getString(R.string.tales));
            startForegroundService(stream);
        }

        setPlayBtnIcon(false);
    }

    private void setPlayBtnIcon() { setPlayBtnIcon(false); }

    private void setPlayBtnIcon(boolean scrollToTale) {
        Tale currentTale = tales.getById(Tales.getNowPlaying());
        boolean isPaused = Tales.isPaused();

        for (Tale tale:tales.getTalesList()) {
            ImageView btn = ItemsList.findViewWithTag(tale.id).findViewById(R.id.play);
            boolean isPlaying = !isPaused && currentTale != null && tale.id == currentTale.id;

            btn.setImageResource(isPlaying ? R.mipmap.tale_pause : R.mipmap.tale_play);
            btn.setTag(isPlaying ? R.mipmap.tale_pause : R.mipmap.tale_play);
        }

        if (scrollToTale && currentTale != null) {
            new TaleView(currentTale).scrollIntoView();
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
        }catch (Exception e) {
            // nothing
        }
    }

    private void unregisterReceiver() {
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e) { /*nothing*/ }
    }

    @Override
    public void onDestroy() {
       unregisterReceiver();
        super.onDestroy();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        switch (intent.getStringExtra("code")) {
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