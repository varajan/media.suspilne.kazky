package media.suspilne.kazky;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlayerRadioAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {
    private Context context;

    public PlayerRadioAdapter(Context context){
        this.context = context;
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        return "Радіо казок";
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        return "Радіо хвиля казок українською мовою";
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        Drawable image = ContextCompat.getDrawable(context, R.mipmap.radio);
        return ImageHelper.getBitmap(image);
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        Intent notificationIntent = new Intent(context, ActivityRadio.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openTracksIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        return openTracksIntent;
    }
}