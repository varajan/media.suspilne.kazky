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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
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
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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
        String type = intent != null ? intent.getStringExtra("type") : "null";
        SettingsHelper.setString("StreamType", type);
        registerReceiver();

        if (type.equals(getString(R.string.radio))){
            playRadio();
            return START_NOT_STICKY;
        }

        if (type.equals(getString(R.string.tales))){
            int taleId = intent != null ? intent.getIntExtra("tale.id", -1) : -1;
            Tale tale = new Tales().getById(taleId);
            playTale(tale);
            return START_NOT_STICKY;
        }

        return START_NOT_STICKY;
    }

    private void playStream(String stream, long position) {
        releasePlayer();

        Uri uri = Uri.parse(stream);
        player = new SimpleExoPlayer.Builder(ActivityMain.getActivity()).build();

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this,"exoplayer-codelab"))
                .createMediaSource(MediaItem.fromUri(uri));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
        player.seekTo(position);

        Tales.setPause(false);

        playerNotificationManager.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                stopSelf();
                sendMessage("SourceIsNotAccessible");
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                Tales.setPause(!playWhenReady);
                sendMessage("SetPlayBtnIcon");
            }

            @Override
            public void onPlaybackStateChanged(@Player.State int playbackState) {
                sendMessage("SetPlayBtnIcon");

                if (playbackState == ExoPlayer.STATE_ENDED) {
                    Tales.setLastPosition(0);
                    playTale(new Tales().getNext());
                }
            }

            @Override
            public void onPositionDiscontinuity(
                    @NonNull Player.PositionInfo oldPosition,
                    @NonNull Player.PositionInfo newPosition,
                    int reason) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    if (oldPosition.positionMs > newPosition.positionMs) {
                        playTale(new Tales().getPrevious());
                    } else {
                        playTale(new Tales().getNext());
                    }
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

            PlayerNotificationManager.NotificationListener listener = new PlayerNotificationManager.NotificationListener() {
                @Override
                public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                    startForeground(notificationId, notification);
                }

                @Override
                public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                    stopSelf();
                }
            };

            playerNotificationManager = new PlayerNotificationManager
                    .Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL)
                    .setNotificationListener(listener)
                    .setMediaDescriptionAdapter(new PlayerTaleAdapter(this))
                    .setStopActionIconResourceId(R.drawable.exo_notification_stop)
                    .build();

            playerNotificationManager.setUseStopAction(true);

            playerNotificationManager.setUseFastForwardAction(true);
            playerNotificationManager.setUseRewindAction     (true);
            playerNotificationManager.setUseNextAction       (false);
            playerNotificationManager.setUsePreviousAction   (false);

            playerNotificationManager.setUseFastForwardActionInCompactView(true);
            playerNotificationManager.setUseRewindActionInCompactView     (true);
            playerNotificationManager.setUseNextActionInCompactView       (false);
            playerNotificationManager.setUsePreviousActionInCompactView   (false);

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
        PlayerNotificationManager.NotificationListener listener = new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                stopSelf();
            }
        };

        playerNotificationManager = new PlayerNotificationManager
                .Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL)
                .setNotificationListener(listener)
                .setMediaDescriptionAdapter(new PlayerRadioAdapter(this))
                .setStopActionIconResourceId(R.drawable.exo_notification_stop)
                .build();
        playerNotificationManager.setUseStopAction(true);

        playStream("https://radio.nrcu.gov.ua:8443/kazka-mp3", 0);
        sendMessage("SetPlayBtnIcon");
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("StopPlay".equals(intent.getStringExtra("code"))) {
                Tales.setNowPlaying(-1);
                sendMessage("SetPlayBtnIcon");
                stopSelf();
            }
        }
    };
}