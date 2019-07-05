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

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import static com.google.android.exoplayer2.ExoPlayerFactory.newSimpleInstance;

public class PlayerService extends Service {
    private ExoPlayer player;
    private NotificationManager notificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String type = intent.getStringExtra("type");
        SettingsHelper.setString("StreamType", type);

        switch (type){
            case "radio":
                playRadio();
                break;

            case "tale":
                playTale(intent.getIntExtra("id", 0));
                break;
        }

        return START_NOT_STICKY;
    }

    private void playStream(String stream) { playStream(stream, 0); }

    private void playStream(String stream, long position) {
        Uri uri = Uri.parse(stream);
        player = newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());

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
        intent.setAction(SettingsHelper.application);
        intent.putExtra("code", code);
        intent.putExtra("id", id);
        sendBroadcast(intent);
    }

    private Notification getTalesNotification(int id, String reader, String title){
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Drawable image = SettingsHelper.getImage(String.format("%02d.jpg", id));

        Intent notificationIntent = new Intent(this, ActivityTales.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openTalesIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, SettingsHelper.application)
                .setSmallIcon(R.drawable.ic_radio)
                .setContentTitle(title)
                .setContentText(reader)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(ImageHelper.getBitmap(image))
                .setUsesChronometer(true)
                .setSound(null)
                .setContentIntent(openTalesIntent);

        Intent playPrevIntent = new Intent();
        playPrevIntent.setAction(SettingsHelper.application + "previous");
        playPrevIntent.putExtra("code", "PlayPrevious");
        PendingIntent playPrevPendingIntent = PendingIntent.getBroadcast(this, 0, playPrevIntent, 0);
        notificationBuilder.addAction(0, "        <<", playPrevPendingIntent);

        Intent stopIntent = new Intent();
        stopIntent.setAction(SettingsHelper.application + "stop");
        stopIntent.putExtra("code", "StopPlay");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        notificationBuilder.addAction(0, "  ||", stopPendingIntent);

        Intent playNextIntent = new Intent();
        playNextIntent.setAction(SettingsHelper.application + "next");
        playNextIntent.putExtra("code", "PlayNext");
        PendingIntent playNextPendingIntent = PendingIntent.getBroadcast(this, 0, playNextIntent, 0);
        notificationBuilder.addAction(0, ">>", playNextPendingIntent);

        return notificationBuilder.build();
    }

    private Notification getRadioNotification(){
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Drawable image = ContextCompat.getDrawable(this, R.mipmap.radio);

        Intent notificationIntent = new Intent(this, ActivityRadio.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openRadioIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, SettingsHelper.application)
                .setSmallIcon(R.drawable.ic_radio)
                .setContentTitle("Радіо казок")
                .setContentText("Радіо хвиля казок українською мовою")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(ImageHelper.getBitmap(image))
                .setSound(null)
                .setContentIntent(openRadioIntent);

        Intent stopIntent = new Intent();
        stopIntent.setAction(SettingsHelper.application + "stop");
        stopIntent.putExtra("code", "StopPlay");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        notificationBuilder.addAction(0, "Зупинити", stopPendingIntent);

        return notificationBuilder.build();
    }

    @Override
    public void onCreate(){
        registerReceiver();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            SettingsHelper.setLong("PlayerPosition", player.getCurrentPosition());
        }

        releasePlayer();
        clearNotifications();
        unregisterReceiver();
    }

    private void releasePlayer(){
        while (player != null){
            player.release();
            player = null;
        }
    }

    private void clearNotifications(){
        if (notificationManager != null){
            notificationManager.cancelAll();
        }
    }

    private void registerReceiver(){
        try{
            IntentFilter filter = new IntentFilter();

            filter.addAction(SettingsHelper.application);
            filter.addAction(SettingsHelper.application + "previous");
            filter.addAction(SettingsHelper.application + "next");
            filter.addAction(SettingsHelper.application + "stop");

            this.registerReceiver(receiver, filter);
        }catch (Exception e){ /*nothing*/ }
    }

    private void unregisterReceiver(){
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e){ /*nothing*/ }
    }

    private void playTale(int id){
        ActivityTales tales = new ActivityTales();

        if (id > 0){
            String name = String.format("%02d.mp3", id);
            String url = "https://kazky.suspilne.media/inc/audio/" + name;
            String stream = SettingsHelper.taleExists(id) ? this.getFilesDir() + "/" + name : url;
            String title = SettingsHelper.getString("title-" + id);
            String reader = SettingsHelper.getString("reader-" + id);
            long position = id == tales.getLastPlaying() ? tales.getPosition() : 0;

            playStream(stream, position);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(SettingsHelper.application, SettingsHelper.application, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setSound(null, null);
                notificationChannel.setShowBadge(false);

                notificationManager.createNotificationChannel(notificationChannel);

                this.startForeground(1, getTalesNotification(id, reader, title));
            } else{
                notificationManager.notify(1, getTalesNotification(id, reader, title));
            }
        }

        tales.setLastPlaying(id);
        tales.setNowPlaying(id);
        sendMessage("SetPlayBtnIcon", id);
    }

    private void playRadio(){
        playStream("https://radio.nrcu.gov.ua:8443/kazka-mp3");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(SettingsHelper.application, SettingsHelper.application, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound(null, null);
            notificationChannel.setShowBadge(false);

            notificationManager.createNotificationChannel(notificationChannel);

            this.startForeground(1, getRadioNotification());
        } else{
            notificationManager.notify(1, getRadioNotification());
        }

        sendMessage("SetPlayBtnIcon", -1);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ActivityTales tales = new ActivityTales();

            switch (intent.getStringExtra("code")){
                case "SourceIsNotAccessible":
                    stopService(new Intent(PlayerService.this, PlayerService.class));
                    break;

                case "MediaIsEnded":
                    releasePlayer();
                    playTale(tales.getNext());
                    break;

                case "PlayNext":
                    releasePlayer();
                    playTale(tales.getNext());
                    break;

                case "PlayPrevious":
                    releasePlayer();
                    playTale(tales.getPrevious());
                    break;

                case "StopPlay":
                    tales.setNowPlaying(-1);
                    tales.setLastPlaying(-1);
                    tales.setPosition(player.getCurrentPosition());

                    sendMessage("SetPlayBtnIcon", -1);
                    stopService(new Intent(PlayerService.this, PlayerService.class));
                    break;
            }
        }
    };
}