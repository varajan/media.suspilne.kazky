package media.suspilne.kazky;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private ImageView playPauseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String url = "https://radio.nrcu.gov.ua:8443/kazka-mp3";

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        playPauseBtn = this.findViewById(R.id.playPause);
        playPauseBtn.setEnabled(false);

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
                return false;
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                playPauseBtn.setEnabled(true);
            }
        });

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) { /* nothing*/
        } catch (IllegalStateException e) {  /* nothing*/
        } catch (IOException e) {  /* nothing*/ }

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    playPauseBtn.setImageResource(R.mipmap.play);
                }else{
                    mediaPlayer.start();
                    playPauseBtn.setImageResource(R.mipmap.pause);
                }
            }
        });
    }
}
