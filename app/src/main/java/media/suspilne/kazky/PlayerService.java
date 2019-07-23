package media.suspilne.kazky;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import static com.google.android.exoplayer2.ExoPlayerFactory.newSimpleInstance;

public class PlayerService extends Service {
    public static String NOTIFICATION_CHANNEL = HSettings.application;
    public static int NOTIFICATION_ID = 2001;

    private ExoPlayer player;
    private NotificationManager notificationManager;
    private PlayerNotificationManager playerNotificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String type = intent.getStringExtra("type");
        HSettings.setString("StreamType", type);
        registerReceiver();

        if (type.equals(getString(R.string.radio))){
            playRadio();
            return START_NOT_STICKY;
        }

        if (type.equals(getString(R.string.tales))){
            playTale(intent.getIntExtra("id", 0));
            return START_NOT_STICKY;
        }

        return START_NOT_STICKY;
    }

    private void playStream() { playStream("https://radio.nrcu.gov.ua:8443/kazka-mp3", 0); }

    private void playStream(String stream, long position) {
        Uri uri = Uri.parse(stream);
        player = newSimpleInstance(this);

        MediaSource mediaSource = new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(this,"exoplayer-codelab"))
                .createMediaSource(uri);
        player.prepare(mediaSource, true, false);
        player.setPlayWhenReady(true);
        if (position > 0) player.seekTo(position);

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {}

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}

            @Override
            public void onLoadingChanged(boolean isLoading) {}

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch(playbackState) {
                    case ExoPlayer.DISCONTINUITY_REASON_SEEK:
                        sendMessage("SourceIsNotAccessible", -1);
                        break;

                    case ExoPlayer.DISCONTINUITY_REASON_INTERNAL:
                        sendMessage("MediaIsEnded", -1);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {}

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}

            @Override
            public void onPlayerError(ExoPlaybackException error) { }

            @Override
            public void onPositionDiscontinuity(int reason) {}

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}

            @Override
            public void onSeekProcessed() {}
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessage(String code, int id){
        Intent intent = new Intent();
        intent.setAction(HSettings.application);
        intent.putExtra("code", code);
        intent.putExtra("id", id);
        sendBroadcast(intent);
    }

    private Notification getTalesNotification(int id, String reader, String title){
        HSettings.setString("StreamType", getString(R.string.tales));

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Drawable image = HSettings.getImage(String.format("%02d.jpg", id));

        Intent notificationIntent = new Intent(this, ActivityTales.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openTalesIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, HSettings.application)
            .setSmallIcon(R.drawable.ic_radio)
            .setContentTitle(title)
            .setContentText(reader)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLargeIcon(HImages.getBitmap(image))
            .setUsesChronometer(true)
            .setSound(null)
            .setChannelId(HSettings.application)
            .setContentIntent(openTalesIntent);

        Intent playPrevIntent = new Intent();
        playPrevIntent.setAction(HSettings.application + "previous");
        playPrevIntent.putExtra("code", "PlayPrevious");
        PendingIntent playPrevPendingIntent = PendingIntent.getBroadcast(this, 0, playPrevIntent, 0);
        notificationBuilder.addAction(0, "<< Назад", playPrevPendingIntent);

        Intent stopIntent = new Intent();
        stopIntent.setAction(HSettings.application + "stop");
        stopIntent.putExtra("code", "StopPlay");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        notificationBuilder.addAction(0, "Зупинити", stopPendingIntent);

        Intent playNextIntent = new Intent();
        playNextIntent.setAction(HSettings.application + "next");
        playNextIntent.putExtra("code", "PlayNext");
        PendingIntent playNextPendingIntent = PendingIntent.getBroadcast(this, 0, playNextIntent, 0);
        notificationBuilder.addAction(0, "Вперед >>", playNextPendingIntent);

        return notificationBuilder.build();
    }

    private Notification getRadioNotification(){
        HSettings.setString("StreamType", getString(R.string.radio));

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Drawable image = ContextCompat.getDrawable(this, R.mipmap.radio);

        Intent notificationIntent = new Intent(this, ActivityRadio.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openRadioIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, HSettings.application)
            .setSmallIcon(R.drawable.ic_radio)
            .setContentTitle("Радіо казок")
            .setContentText("Радіо хвиля казок українською мовою")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLargeIcon(HImages.getBitmap(image))
            .setSound(null)
            .setChannelId(HSettings.application)
            .setContentIntent(openRadioIntent);

        Intent stopIntent = new Intent();
        stopIntent.setAction(HSettings.application + "stop");
        stopIntent.putExtra("code", "StopPlay");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        notificationBuilder.addAction(0, "Зупинити", stopPendingIntent);

        return notificationBuilder.build();
    }

    @Override
    public void onCreate(){
        registerReceiver();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(HSettings.application);

            if (channel == null){
                NotificationChannel notificationChannel = new NotificationChannel(HSettings.application, HSettings.application, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setSound(null, null);
                notificationChannel.setShowBadge(false);

                notificationManager.createNotificationChannel(notificationChannel);
            }

            this.startForeground(NOTIFICATION_ID, getRadioNotification());
        }
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll();
        releasePlayer();
        unregisterReceiver();
        super.onDestroy();
    }

    private void releasePlayer(){
        HSettings.setString("StreamType", "");
        notificationManager.cancelAll();

        while (player != null){
            ActivityTales.setPosition(player.getCurrentPosition());
            player.release();
            player = null;
        }
    }

    private void registerReceiver(){
        try{
            IntentFilter filter = new IntentFilter();

            filter.addAction(HSettings.application);
            filter.addAction(HSettings.application + "previous");
            filter.addAction(HSettings.application + "next");
            filter.addAction(HSettings.application + "stop");

            this.registerReceiver(receiver, filter);
        }catch (Exception e){ /*nothing*/ }
    }

    private void unregisterReceiver(){
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e){ /*nothing*/ }
    }

    private void playTale(int id){
        if (id > 0){
            String name = String.format("%02d.mp3", id);
            String url = "https://kazky.suspilne.media/inc/audio/" + name;
            String stream = HSettings.taleExists(id) ? this.getFilesDir() + "/" + name : url;
            String title = HSettings.getString("title-" + id);
            String reader = HSettings.getString("reader-" + id);
            long position = id == ActivityTales.getLastPlaying() ? ActivityTales.getPosition() : 0;
            playStream(stream, position);

            playerNotificationManager = new PlayerNotificationManager(this, NOTIFICATION_CHANNEL, NOTIFICATION_ID, new PlayerTaleAdapter(this, id));
            playerNotificationManager.setFastForwardIncrementMs(10_000_000);
            playerNotificationManager.setRewindIncrementMs(10_000_000);
            playerNotificationManager.setUseNavigationActions(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForeground(NOTIFICATION_ID, getTalesNotification(id, reader, title));
            } else{
                notificationManager.notify(NOTIFICATION_ID, getTalesNotification(id, reader, title));
            }
        }

        ActivityTales.setLastPlaying(id);
        ActivityTales.setNowPlaying(id);
        sendMessage("SetPlayBtnIcon", id);
    }

    private void playRadio(){
        playerNotificationManager = new PlayerNotificationManager(this, NOTIFICATION_CHANNEL, NOTIFICATION_ID, new PlayerRadioAdapter(this));
        playerNotificationManager.setFastForwardIncrementMs(0);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setUseNavigationActions(false);

        playStream();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForeground(NOTIFICATION_ID, getRadioNotification());
        } else{
            notificationManager.notify(NOTIFICATION_ID, getRadioNotification());
        }

        sendMessage("SetPlayBtnIcon", -1);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("code")){
//                case "SourceIsNotAccessible":
//                    stopSelf();
//                    break;
//
//                case "MediaIsEnded":
//                    releasePlayer();
//                    playTale(ActivityTales.getNext());
//                    break;
//
//                case "PlayNext":
//                    releasePlayer();
//                    playTale(ActivityTales.getNext());
//                    break;
//
//                case "PlayPrevious":
//                    releasePlayer();
//                    playTale(ActivityTales.getPrevious());
//                    break;

                case "StopPlay":
                    ActivityTales.setNowPlaying(-1);

                    sendMessage("SetPlayBtnIcon", -1);
                    stopSelf();
                    break;
            }
        }
    };
}