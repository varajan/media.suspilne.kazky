package media.suspilne.kazky;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActivityColorings extends ActivityMain {
    private LinearLayout TalesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.coloring_menu;
        super.onCreate(savedInstanceState);

        TalesList = findViewById(R.id.talesList);
        showTales();
    }

    private void showTales(){
        boolean showBigImages = SettingsHelper.getBoolean("showBigImages");
        Tales tales = new Tales();

        for (final Tale tale:tales.items) {
            if (tale.coloring == 0) continue;

            View taleView = LayoutInflater.from(this).inflate(showBigImages ? R.layout.tale_item : R.layout.tale_item_small, TalesList, false);
            taleView.setTag(tale.id);
            TalesList.addView(taleView);
            tale.setColoringDetails(showBigImages);

            taleView.findViewById(R.id.play).setOnClickListener(v -> {
                if (!this.isNetworkAvailable()){
                    Toast.makeText(getActivity(), R.string.no_internet ,Toast.LENGTH_LONG).show();
                    return;
                }

                String url = ActivityColorings.getActivity().getResources().getString(R.string.coloringUrl, tale.coloring);
                String fileName = String.format("kazka-%d.pdf", tale.coloring);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.allowScanningByMediaScanner();
                DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                manager.enqueue(request);

                Toast.makeText(getActivity(), R.string.coloring_download ,Toast.LENGTH_LONG).show();
            });
        }

        findViewById(R.id.nothingToShow).setVisibility(View.GONE);
        findViewById(R.id.searchIcon).setVisibility(View.GONE);
        findViewById(R.id.showFavorite).setVisibility(View.GONE);
    }
}