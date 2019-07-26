package media.suspilne.kazky;

import android.app.Application;
import android.content.SharedPreferences;
import com.google.android.gms.security.ProviderInstaller;
import javax.net.ssl.SSLContext;

public class ApplicationKazky extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UpdateSslProvider();

        SharedPreferences.Editor editor = getSharedPreferences(HSettings.application, 0).edit();
        editor.putString("StreamType", "");
        editor.putString("playbackIsPaused", String.valueOf(false));
        editor.putString("nowPlaying", String.valueOf(-1));
        editor.putString("lastPlaying", String.valueOf(-1));
        editor.putString("errorMessage", "");
        editor.apply();
    }

    public void UpdateSslProvider(){
        try {
            ProviderInstaller.installIfNeeded(this);

            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
