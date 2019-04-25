package media.suspilne.kazky;

import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.security.ProviderInstaller;

import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;

public class Radio extends AppCompatActivity {
    private ExoPlayer player;
    private ImageView playPauseBtn;
    private Timer quitTimer;

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        playPauseBtn.setImageResource(R.mipmap.play);
    }

    private void initializePlayer() {
        Uri uri = Uri.parse("https://radio.nrcu.gov.ua:8443/kazka-mp3");

        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

        MediaSource mediaSource = new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab"))
                .createMediaSource(uri);
        player.prepare(mediaSource, true, false);
        player.setPlayWhenReady(true);
        playPauseBtn.setImageResource(R.mipmap.pause);
    }

    private void UpdateSslProvider(){
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());

            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSettingsView(){
        startActivityForResult(new Intent(Radio.this, Settings.class), 0);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                openSettingsView();
                return true;

            case KeyEvent.KEYCODE_BACK:
                System.exit(1);
                return true;
        }

        return super.onKeyDown(keycode, e);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;

        setQuiteTimeout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        UpdateSslProvider();

        this.findViewById(R.id.menuBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { openSettingsView(); }
                }
        );

        this.findViewById(R.id.listBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        releasePlayer();
                        startActivityForResult(new Intent(Radio.this, Tales.class), 0);
                    }
                }
        );

        playPauseBtn = this.findViewById(R.id.playPause);
        playPauseBtn.setImageResource(R.mipmap.play);
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player == null){
                    initializePlayer();
                }else{
                    releasePlayer();
                }
            }
        });

        if (SettingsHelper.getBoolean(this, "autoStart")) {
            initializePlayer();
        }

        setQuiteTimeout();
    }

    private void setQuiteTimeout(){
        if (SettingsHelper.getBoolean(this, "autoStop")) {
            if (quitTimer!=null) quitTimer.cancel();
            int timeout = SettingsHelper.getInt(this, "timeout");

            quitTimer = new Timer();
            quitTimer.schedule(new stopRadioOnTimeout(), timeout * 60 * 1000);
        } else {
            if (quitTimer!=null) quitTimer.cancel();
        }
    }

    class stopRadioOnTimeout extends TimerTask {
        @Override
        public void run() {
            System.exit(1);
        }
    }
}