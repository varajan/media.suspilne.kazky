package media.suspilne.kazky;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Radio extends MainActivity {
    private ImageView playPauseBtn;
    private String radioStream = "https://radio.nrcu.gov.ua:8443/kazka-mp3";

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (player.isPlaying())
//            player.releasePlayer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putBoolean("isPlaying", player.isPlaying());
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
        setContentView(R.layout.activity_radio);
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ((ImageView) findViewById(R.id.onlinePlayer)).setImageResource(R.mipmap.online_player);

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

//        player.addListener(new Player.SourceIsNotAccessibleListener(){
//            @Override
//            public void sourceIsNotAccessible(){
//                playPauseBtn.setImageResource(R.mipmap.play);
//                player.releasePlayer();
//
//                Toast.makeText(Radio.this, "Відсутній Інтернет!", Toast.LENGTH_LONG).show();
//            }
//        });
    }
}