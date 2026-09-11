package media.suspilne.kazky;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.stream.Collectors;

public class ActivityColorings extends ActivityMain {
    private Tales tales;
    private LinearLayout TalesList;
    private TextView titleFld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.coloring_menu;
        activityName = this.getString(R.string.coloring);
        super.onCreate(savedInstanceState);

        tales  = new Tales();
        titleFld = findViewById(R.id.title);
        TalesList = findViewById(R.id.itemsList);
        FloatingActionButton searchBtn = findViewById(R.id.searchBtn);

        categories = Categories.Items.stream()
                .filter(c -> c.taleIds.stream()
                        .anyMatch(t -> tales.getById(t).coloring > 0))
                .map(c -> c.title)
                .toList();
        
        searchBtn.setOnClickListener(v -> showFilterDialog(this::filterTales));
        titleFld.setOnClickListener(v -> showFilterDialog(this::filterTales));
        boolean nothingToShow = SettingsHelper.getInt(activityName) == View.VISIBLE;

        showTales();
        if (nothingToShow) resetFilter();
        filterTales();
    }

    private void filterTales() {
        View nothing = findViewById(R.id.nothingToShow);
        AppCompatButton showAllBtn = findViewById(R.id.showAllBtn);
        int nothingToShowVisibility = View.VISIBLE;
        StringBuilder list = new StringBuilder();

        String filter = Tales.getFilter(activityName);
        boolean showOnlyFavorite = Tales.getShowOnlyFavorite(activityName);
        List<Integer> selectedCategories = categories.stream()
                .filter(category -> Tales.getShowCategory(activityName, this.getResourceString(category)))
                .collect(Collectors.toList());

        for (final Tale tale:tales.getTalesList()) {
            if (tale.coloring == 0) continue;

            if (tale.shouldBeShown(showOnlyFavorite, categories, filter)) {
                tale.show();
                nothingToShowVisibility = View.GONE;
                list.append(tale.id).append(";");
            } else {
                tale.hide();
            }
        }

        boolean hideSearchText = !showOnlyFavorite && filter.isEmpty() && selectedCategories.equals(categories);
        titleFld.setText(hideSearchText ? this.getText(R.string.coloring) : getSearchFieldText(categories));
        nothing.setVisibility(nothingToShowVisibility);
        showAllBtn.setVisibility(nothingToShowVisibility);
        showAllBtn.setOnClickListener(v -> resetFilter(this::filterTales));

        SettingsHelper.setInt(activityName, nothingToShowVisibility);
    }

    private void showTales() {
        boolean showBigImages = SettingsHelper.getBoolean("showBigImages");

        for (final Tale tale:tales.getTalesList()) {
            if (tale.coloring == 0) continue;

            View taleView = LayoutInflater.from(this).inflate(showBigImages ? R.layout.tale_item : R.layout.tale_item_small, TalesList, false);
            taleView.setTag(tale.id);
            TalesList.addView(taleView);
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