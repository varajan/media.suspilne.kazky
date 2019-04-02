package media.suspilne.kazky;

import android.app.ProgressDialog;
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
    private ProgressDialog progress;

    private void Prepare(){
        String url = "https://radio.nrcu.gov.ua:8443/kazka-mp3";

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                playPauseBtn.setImageResource(R.mipmap.pause);
                playPauseBtn.setEnabled(true);
                progress.dismiss();
                mediaPlayer.start();
            }
        });

        progress = new ProgressDialog(this);
        progress.setTitle("Радіо казки");
        progress.setMessage("Почекай...");
        progress.setCancelable(false);
        progress.show();

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
                return false;
            }
        });

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {  /* nothing*/ }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playPauseBtn = this.findViewById(R.id.playPause);
        playPauseBtn.setEnabled(false);

        Prepare();

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
