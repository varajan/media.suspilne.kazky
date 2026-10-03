package media.suspilne.kazky.tasks;

import android.os.AsyncTask;

import com.google.android.gms.common.util.IOUtils;

import java.io.InputStream;
import java.net.URL;

import media.suspilne.kazky.activities.views.TaleView;
import media.suspilne.kazky.data.Tale;
import media.suspilne.kazky.helpers.SettingsHelper;

public class DownloadTaleTask extends AsyncTask<Tale, Void, Void> {
    private Tale tale;

    @Override
    protected void onPostExecute(Void result) {
        TaleView taleView = new TaleView(tale);
        taleView.setDownloadedIcon();
    }

    @Override
    protected Void doInBackground(Tale... tales) {
        try {
            tale = tales[0];
            if (!tale.isDownloaded)
            {
                InputStream is = (InputStream) new URL(tale.stream).getContent();
                SettingsHelper.saveFile(tale.fileName, IOUtils.toByteArray(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
