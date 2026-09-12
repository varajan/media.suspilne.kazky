package media.suspilne.kazky.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

import media.suspilne.kazky.data.Categories;
import media.suspilne.kazky.R;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.data.Tale;
import media.suspilne.kazky.data.Tales;

public class ActivityColorings extends ListActivity {
    private Tales tales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.coloring_menu;
        activityName = defaultTitleText = this.getString(R.string.coloring);
        super.onCreate(savedInstanceState);

        tales  = new Tales();
        titleFld = findViewById(R.id.title);
        initControls();

        categories = Categories.Items.stream()
                .filter(c -> c.taleIds.stream()
                        .anyMatch(t -> tales.getById(t).coloring > 0))
                .map(c -> c.title)
                .toList();
        
        searchBtn.setOnClickListener(v -> showFilterDialog(() -> applyFilter(this::filterTales)));
        titleFld.setOnClickListener(v -> showFilterDialog(() -> applyFilter(this::filterTales)));
        boolean nothingToShow = SettingsHelper.getInt(activityName) == View.VISIBLE;

        showTales();
        if (nothingToShow) resetFilter();
        applyFilter(this::filterTales);
    }

    private int filterTales(String filter, boolean showOnlyFavorite, List<Integer> categories) {
        int nothingToShowVisibility = View.VISIBLE;

        for (final Tale tale:tales.getTalesList()) {
            if (tale.shouldBeShown(showOnlyFavorite, categories, filter)) {
                tale.show();
                nothingToShowVisibility = View.GONE;
            } else {
                tale.hide();
            }
        }

        return nothingToShowVisibility;
    }

    private void showTales() {
        boolean showBigImages = SettingsHelper.getBoolean("showBigImages");

        for (final Tale tale:tales.getTalesList()) {
            if (tale.coloring == 0) continue;

            View taleView = LayoutInflater.from(this).inflate(showBigImages ? R.layout.tale_item : R.layout.tale_item_small, ItemsList, false);
            taleView.setTag(tale.id);
            ItemsList.addView(taleView);
            tale.setColoringDetails(showBigImages);

            taleView.findViewById(R.id.play).setOnClickListener(v -> {
                if (this.isNetworkUnavailable()) {
                    Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_LONG).show();
                    return;
                }

                String url = ActivityColorings.getActivity().getResources().getString(R.string.coloringUrl, tale.id);
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.logo)
                        .setTitle(R.string.coloring_download_ask)
                        .setPositiveButton(R.string.yes, (dialog, which) -> download(url, tale.getTitle() + ".jpg"))
                        .setNegativeButton(R.string.no, null)
                        .show();
            });
        }
    }

    private void download(String url, String fileName) {
        Toast.makeText(getActivity(), R.string.coloring_download, Toast.LENGTH_LONG).show();

        try{
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            downloadManager.enqueue(request);
        } catch (Exception ex) {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }
}