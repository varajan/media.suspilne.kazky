package media.suspilne.kazky;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;

public class Radio extends AppCompatActivity {
    private ImageView playPauseBtn;
    private Timer quitTimer;
    private Player player;
    private String radioStream = "https://radio.nrcu.gov.ua:8443/kazka-mp3";

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
                    startActivityForResult(new Intent(Radio.this, Tales.class), 0);
                }
            }
        );

        playPauseBtn = this.findViewById(R.id.playPause);
        playPauseBtn.setImageResource(R.mipmap.play);
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (player == null){
                player.initializePlayer(radioStream);
                playPauseBtn.setImageResource(R.mipmap.pause);
            }else{
                player.releasePlayer();
                playPauseBtn.setImageResource(R.mipmap.play);
            }
            }
        });

        if (SettingsHelper.getBoolean(this, "autoStart")) {
            player.initializePlayer(radioStream);
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