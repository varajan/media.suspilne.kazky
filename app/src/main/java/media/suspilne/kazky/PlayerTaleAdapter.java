package media.suspilne.kazky;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlayerTaleAdapter implements PlayerNotificationManager.MediaDescriptionAdapter{
    private Context context;

    PlayerTaleAdapter(Context context) {
        this.context = context;
    }

    private Tale tale(){
        return new Tales().getById(Tales.getNowPlaying());
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        try {
            return tale().getTitle();
        }
        catch (Exception e) {
            return context.getResources().getString(R.string.title);
        }
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        try {
            return tale().getReader();
        }
        catch (Exception e) {
            return context.getResources().getString(R.string.reader);
        }
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        try {
            return ImageHelper.getBitmapFromResource(context.getResources(), tale().image, 100, 75);
        }
        catch (Exception e) {
            return ImageHelper.getBitmapFromResource(context.getResources(), R.drawable.t001, 100, 75);
        }
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        Intent notificationIntent = new Intent(context, ActivityTales.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getActivity(context, 0, notificationIntent, 0);
    }
}