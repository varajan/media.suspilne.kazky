package media.suspilne.kazky;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button playPauseBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String url = "https://radio.nrcu.gov.ua:8443/kazka-mp3";

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        playPauseBtn = this.findViewById(R.id.PlayPause);
        playPauseBtn.setEnabled(false);
        playPauseBtn.setText("...");
        Toast.makeText(getBaseContext(), "Radio is opening...", Toast.LENGTH_LONG).show();

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
                playPauseBtn.setText("Pause");
            }
        });

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPlayClick(android.view.View view){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            playPauseBtn.setText("Play");
            Toast.makeText(getBaseContext(), "Radio is paused", Toast.LENGTH_SHORT).show();
        }else{
            mediaPlayer.start();
            playPauseBtn.setText("Pause");
            Toast.makeText(getBaseContext(), "Radio is opening...", Toast.LENGTH_SHORT).show();
        }
    }
}
