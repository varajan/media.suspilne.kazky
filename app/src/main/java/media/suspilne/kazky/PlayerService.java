package media.suspilne.kazky;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

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
        HSettings.setBoolean("playbackIsPaused", false);

        playerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });
        playerNotificationManager.setPlayer(player);

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {}

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}

            @Override
            public void onLoadingChanged(boolean isLoading) {}

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                HSettings.setBoolean("playbackIsPaused", !playWhenReady);
                sendMessage("SetPlayBtnIcon", HSettings.getInt("nowPlaying"));

                switch(playbackState) {
                    case ExoPlayer.DISCONTINUITY_REASON_SEEK:
//                        Tracks.setNowPlaying(-1);
//                        Tracks.setLastPosition(player.getCurrentPosition());
                        stopSelf();
                        sendMessage("SetPlayBtnIcon", -1);
                        break;

                    case ExoPlayer.DISCONTINUITY_REASON_INTERNAL:
                        playTale(ActivityTales.getNext());
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
            public void onPlayerError(ExoPlaybackException error) {
                stopSelf();
                sendMessage("SourceIsNotAccessible", -1);
            }

            @Override
            public void onPositionDiscontinuity(int reason) {}

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}

            @Override
            public void onSeekProcessed() {
                long position = player.getCurrentPosition();
                long duration = player.getContentDuration();

                if (position < 0) {
                    playTale(ActivityTales.getPrevious());
                } else if(position > duration && duration > 0){
                    playTale(ActivityTales.getNext());
                }
            }
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
        }
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(NOTIFICATION_ID);
        HSettings.setString("StreamType", "");
        releasePlayer();
        unregisterReceiver();
        super.onDestroy();
    }

    private void releasePlayer(){
        notificationManager.cancel(NOTIFICATION_ID);

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
            releasePlayer();

            String name = String.format("%02d.mp3", id);
            String url = "https://kazky.suspilne.media/inc/audio/" + name;
            String stream = HSettings.taleExists(id) ? this.getFilesDir() + "/" + name : url;
            long position = id == ActivityTales.getLastPlaying() ? ActivityTales.getPosition() : 0;

            if (!HSettings.taleExists(id) && !HSettings.isNetworkAvailable()){
                sendMessage("SourceIsNotAccessible", -1);
            }

            playerNotificationManager = new PlayerNotificationManager(this, NOTIFICATION_CHANNEL, NOTIFICATION_ID, new PlayerTaleAdapter(this, id));
            playerNotificationManager.setFastForwardIncrementMs(1_000_000);
            playerNotificationManager.setRewindIncrementMs(1_000_000);
            playerNotificationManager.setUseNavigationActions(false);

            playStream(stream, position);
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
        playerNotificationManager.setStopAction(null);
        playerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });

        if (!HSettings.isNetworkAvailable()){
            sendMessage("SourceIsNotAccessible", -1);
        }

        playStream();
        sendMessage("SetPlayBtnIcon", -1);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("code")){
                case "StopPlay":
                    ActivityTales.setNowPlaying(-1);
                    sendMessage("SetPlayBtnIcon", -1);
                    stopSelf();
                    break;
            }
        }
    };
}