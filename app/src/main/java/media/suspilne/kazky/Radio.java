package media.suspilne.kazky;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

public class Radio extends MainActivity {
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
        setContentView(R.layout.activity_radio);
        currentView = R.id.radio_menu;
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ((ImageView) findViewById(R.id.onlinePlayer)).setImageResource(R.mipmap.online_player);

        playPauseBtn = findViewById(R.id.playPause);
        playPauseBtn.setImageResource(R.mipmap.play);
        playPauseBtn.setOnClickListener(v -> {
            if (player.isPlaying()){
                player.releasePlayer();
                playPauseBtn.setImageResource(R.mipmap.play);
            }else{
                if (isNetworkAvailable()){
                    player.initializePlayer(radioStream);
                    playPauseBtn.setImageResource(R.mipmap.pause);
                    setQuiteTimeout();
                }else{
                    showNoConnectionAlert();
              }
            }
        });

        player.addListener((Player.SourceIsNotAccessibleListener) () -> {
            playPauseBtn.setImageResource(R.mipmap.play);
            player.releasePlayer();
            showNoConnectionAlert();
        });
    }

    private void showNoConnectionAlert(){
        new AlertDialog.Builder(Radio.this)
            .setIcon(R.mipmap.logo)
            .setTitle("Відсутній Інтернет!")
            .setMessage("Щоб схухати радіо потрібно підключення до Інтернета.")
            .setPositiveButton("OK", null)
            .show();
    }
}