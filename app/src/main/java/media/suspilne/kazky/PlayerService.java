package media.suspilne.kazky;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;

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

public class PlayerService extends IntentService {
    private ExoPlayer player;
    private NotificationManager notificationManager;
    private PlayerNotificationManager playerNotificationManager;

    public static String NOTIFICATION_CHANNEL = SettingsHelper.application;
    public static int NOTIFICATION_ID = 21;

    public PlayerService() {
        super(NOTIFICATION_CHANNEL);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // not implemented
    }
    
    @Override
    public void onCreate(){
        registerReceiver();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(SettingsHelper.application);

            if (channel == null){
                NotificationChannel notificationChannel = new NotificationChannel(SettingsHelper.application, SettingsHelper.application, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setSound(null, null);
                notificationChannel.setShowBadge(false);

                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String type = intent.getStringExtra("type");
        SettingsHelper.setString("StreamType", type);
        registerReceiver();

        if (type.equals(getString(R.string.radio))){
            playRadio();
            return START_NOT_STICKY;
        }

        if (type.equals(getString(R.string.tales))){
            Tale tale = new Tales().getById(intent.getIntExtra("tale.id", -1));
            playTale(tale);
            return START_NOT_STICKY;
        }

        return START_NOT_STICKY;
    }

    private void playStream(String stream, long position) {
        releasePlayer();

        Uri uri = Uri.parse(stream);
        player = newSimpleInstance(this);

        MediaSource mediaSource = new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(this,"exoplayer-codelab"))
                .createMediaSource(uri);

        player.prepare(mediaSource, true, false);
        player.setPlayWhenReady(true);
        player.seekTo(position);

        Tales.setPause(false);

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
            public void onTracksChanged(TrackGroupArray taleGroups, TrackSelectionArray trackSelections) {}

            @Override
            public void onLoadingChanged(boolean isLoading) {}

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Tales.setPause(!playWhenReady);
                sendMessage("SetPlayBtnIcon");

                switch(playbackState) {
                    case ExoPlayer.DISCONTINUITY_REASON_SEEK:
                        Tales.setNowPlaying(-1);
                        Tales.setLastPosition(player.getCurrentPosition());
                        stopSelf();
                        sendMessage("SetPlayBtnIcon");
                        break;

                    case ExoPlayer.DISCONTINUITY_REASON_INTERNAL:
                        Tales.setLastPosition(0);
                        playTale(new Tales().getNext());
                        break;

                    case ExoPlayer.DISCONTINUITY_REASON_AD_INSERTION:
                        sendMessage("SetPlayBtnIcon");
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
                sendMessage("SourceIsNotAccessible");
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
                    playTale(new Tales().getPrevious());
                } else if(position > duration && duration > 0){
                    playTale(new Tales().getNext());
                }
            }
        });
    }

    private void sendMessage(String code){
        Intent intent = new Intent();
        intent.setAction(SettingsHelper.application);
        intent.putExtra("code", code);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            Tales.setLastPosition(player.getCurrentPosition());
        }

        if (playerNotificationManager != null) playerNotificationManager.setPlayer(null);
        releasePlayer();
        unregisterReceiver();
    }

    private void releasePlayer(){
        while (player != null){
            player.release();
            player = null;
        }
    }

    private void playTale(Tale tale){
        if (tale.id != -1 && !SettingsHelper.getBoolean(("stopPlaybackOnTimeout"))){
            long position = tale.id == Tales.getLastPlaying() ? Tales.getLastPosition() : 0;
            position = SettingsHelper.getBoolean("skipIntro") ? Math.max(position, tale.introTime) : position;

            Tales.setNowPlaying(tale.id);
            Tales.setLastPlaying(tale.id);

            playerNotificationManager = new PlayerNotificationManager(this, NOTIFICATION_CHANNEL, NOTIFICATION_ID, new PlayerTaleAdapter(this));
            playerNotificationManager.setFastForwardIncrementMs(10_000_000);
            playerNotificationManager.setRewindIncrementMs(10_000_000);
            playerNotificationManager.setUseNavigationActions(false);

            playStream(tale.stream, position);
        } else {
            Tales.setNowPlaying(-1);

            playerNotificationManager.setPlayer(null);
            releasePlayer();
        }

        sendMessage("SetPlayBtnIcon");
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

    private void playRadio(){
        playerNotificationManager = new PlayerNotificationManager(this, NOTIFICATION_CHANNEL, NOTIFICATION_ID, new PlayerRadioAdapter(this));
        playerNotificationManager.setFastForwardIncrementMs(0);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setUseNavigationActions(false);
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

        playStream("https://radio.nrcu.gov.ua:8443/kazka-mp3", 0);
        sendMessage("SetPlayBtnIcon");
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("code")){
                case "StopPlay":
                    Tales.setNowPlaying(-1);
                    sendMessage("SetPlayBtnIcon");
                    stopSelf();
                    break;
            }
        }
    };
}