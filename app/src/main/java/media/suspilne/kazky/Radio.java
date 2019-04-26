package media.suspilne.kazky;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Radio extends BaseActivity {
    private ImageView playPauseBtn;
    private Player player;
    private String radioStream = "https://radio.nrcu.gov.ua:8443/kazka-mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        player = new Player(this);
        player.UpdateSslProvider();

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

        playPauseBtn = this.findViewById(R.id.playPause);
        playPauseBtn.setImageResource(R.mipmap.play);
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (player.isPlaying()){
                player.releasePlayer();
                playPauseBtn.setImageResource(R.mipmap.play);
            }else{
                player.initializePlayer(radioStream);
                playPauseBtn.setImageResource(R.mipmap.pause);
            }
            }
        });
    }
}