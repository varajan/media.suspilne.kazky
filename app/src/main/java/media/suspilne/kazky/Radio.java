package media.suspilne.kazky;

import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import javax.net.ssl.SSLContext;

public class Radio extends AppCompatActivity {
    private ExoPlayer player;
    private ImageView playPauseBtn;
    private boolean autoStart;

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        playPauseBtn.setImageResource(R.mipmap.play);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return
            new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab"))
                    .createMediaSource(uri);
    }

    private void initializePlayer() {
        Uri uri = Uri.parse("https://radio.nrcu.gov.ua:8443/kazka-mp3");

        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

        MediaSource mediaSource = buildMediaSource(uri);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        UpdateSslProvider();

        this.findViewById(R.id.menuBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        {
                           startActivity(new Intent(Radio.this, Settings.class));
                        }
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

        autoStart = SettingsHelper.getBoolean(this, "autoStart");
        if (autoStart) {
            initializePlayer();
        }
    }
}