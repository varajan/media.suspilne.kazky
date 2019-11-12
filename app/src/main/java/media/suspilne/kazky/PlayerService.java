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
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.taleselection.taleselectionArray;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

public class PlayerService extends IntentService {
    private ExoPlayer player;
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

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        playerNotificationManager = new PlayerNotificationManager(this, NOTIFICATION_CHANNEL, NOTIFICATION_ID, new PlayerAdapter(this));

        playerNotificationManager.setFastForwardIncrementMs(10_000_000);
        playerNotificationManager.setRewindIncrementMs(10_000_000);
        playerNotificationManager.setUseNavigationActions(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound(null, null);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        TrackEntry track = new tales().getById(intent.getIntExtra("track.id", -1));
        playTrack(track);

        return START_NOT_STICKY;
    }

    private void playStream(String stream, long position) {
        releasePlayer();

        Uri uri = Uri.parse(stream);
        player = ExoPlayerFactory.newSimpleInstance(this);

        MediaSource mediaSource = new ExtractorMediaSource.Factory(
            new DefaultDataSourceFactory(this,"exoplayer-codelab")).createMediaSource(uri);
        player.prepare(mediaSource, true, false);
        player.setPlayWhenReady(true);
        player.seekTo(position);

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
            public void ontalesChanged(TrackGroupArray trackGroups, taleselectionArray taleselections) {}

            @Override
            public void onLoadingChanged(boolean isLoading) {}

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                tales.setPause(!playWhenReady);
                sendMessage("SetPlayBtnIcon");

                switch(playbackState) {
                    case ExoPlayer.DISCONTINUITY_REASON_SEEK:
                        tales.setNowPlaying(-1);
                        tales.setLastPosition(player.getCurrentPosition());
                        stopSelf();
                        sendMessage("SetPlayBtnIcon");
                        break;

                    case ExoPlayer.DISCONTINUITY_REASON_INTERNAL:
                        playTrack(new tales().getNext());
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
            public void onPositionDiscontinuity(int reason) { }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}

            @Override
            public void onSeekProcessed() {
                long position = player.getCurrentPosition();
                long duration = player.getContentDuration();
                tales tales = new tales();

                if (position < 0) {
                    playTrack(tales.getPrevious());
                } else if(position > duration && duration > 0){
                    playTrack(tales.getNext());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            tales.setLastPosition(player.getCurrentPosition());
        }

        playerNotificationManager.setPlayer(null);
        releasePlayer();
        unregisterReceiver();
    }

    private void releasePlayer(){
        while (player != null){
            player.release();
            player = null;
        }
    }

    private void sendMessage(String code){
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_CHANNEL);
        intent.putExtra("code", code);
        sendBroadcast(intent);
    }

    private void playTrack(TrackEntry track){
        if (track.id != -1){
            long position = track.id == tales.getLastPlaying() ? tales.getLastPosition() : 0;

            SettingsHelper.setInt("tales.nowPlaying", track.id);
            SettingsHelper.setInt("tales.lastPlaying", track.id);

            playStream(track.stream, position);
        } else {
            SettingsHelper.setInt("tales.nowPlaying", -1);

            playerNotificationManager.setPlayer(null);
            releasePlayer();
        }

        sendMessage("SetPlayBtnIcon");
    }

    private void registerReceiver(){
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(NOTIFICATION_CHANNEL);
            this.registerReceiver(receiver, filter);
        }catch (Exception e){ /*nothing*/ }
    }

    private void unregisterReceiver(){
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e){ /*nothing*/ }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("code")){
                case "StopPlay":
                    tales.setNowPlaying(-1);
                    tales.setLastPosition(player.getCurrentPosition());
                    stopSelf();
                    sendMessage("SetPlayBtnIcon");
                    break;
            }
        }
    };
}