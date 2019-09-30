package media.suspilne.kazky;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlayerTaleAdapter implements PlayerNotificationManager.MediaDescriptionAdapter{
    private Context context;
    private int id;

    PlayerTaleAdapter(Context context, int id) {
        this.context = context;
        this.id = id;
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        return HStrings.startFromCapital(HSettings.getString("title-" + id));
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        return HStrings.startFromCapital(HSettings.getString("reader-" + id));
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        Drawable image = HSettings.getImage(String.format("%02d.jpg", id));
        return HImages.getBitmap(image);
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        Intent notificationIntent = new Intent(context, ActivityTales.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getActivity(context, 0, notificationIntent, 0);
    }
}
