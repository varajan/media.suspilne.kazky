package media.suspilne.kazky;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Radio extends BaseActivity {
    private ImageView playPauseBtn;
    private String radioStream = "https://radio.nrcu.gov.ua:8443/kazka-mp3";

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying())
            player.releasePlayer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("isPlaying", player.isPlaying());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.getBoolean("isPlaying")){
            playPauseBtn.performClick();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

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
                    player.releasePlayer();
                    startActivity(new Intent(Radio.this, Tales.class));
                }
            }
        );

        playPauseBtn = findViewById(R.id.playPause);
        playPauseBtn.setImageResource(R.mipmap.play);
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (player.isPlaying()){
                player.releasePlayer();
                playPauseBtn.setImageResource(R.mipmap.play);
            }else{
                player.initializePlayer(radioStream);
                playPauseBtn.setImageResource(R.mipmap.pause);
                setQuiteTimeout();
            }
            }
        });

        player.addListener(new Player.SourceIsNotAccessibleListener(){
            @Override
            public void sourceIsNotAccessible(){
                playPauseBtn.setImageResource(R.mipmap.play);
                player.releasePlayer();

                Toast.makeText(Radio.this, "Відсутній Інтернет!", Toast.LENGTH_LONG).show();
            }
        });

    }
}