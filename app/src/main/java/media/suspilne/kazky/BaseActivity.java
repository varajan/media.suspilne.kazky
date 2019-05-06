package media.suspilne.kazky;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

public class BaseActivity extends AppCompatActivity {
    private Timer quitTimer;
    protected Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setQuiteTimeout();

        player = new Player(this);
        player.UpdateSslProvider();
    }

    public void openSettingsView(){
        startActivityForResult(new Intent(this, Settings.class), 0);
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

    protected void setQuiteTimeout(){
        if (SettingsHelper.getBoolean(this, "autoQuit")) {
            if (quitTimer != null) quitTimer.cancel();
            int timeout = SettingsHelper.getInt(this, "timeout");

            quitTimer = new Timer();
            quitTimer.schedule(new stopRadioOnTimeout(), timeout * 60 * 1000);
        } else {
            if (quitTimer != null) quitTimer.cancel();
        }
    }

    class stopRadioOnTimeout extends TimerTask {
        @Override
        public void run() {
            player.releasePlayer();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTalesPlayBtnIcons();
                    updateRadioPlayBtnIcon();
                }
            });
        }

        private void updateTalesPlayBtnIcons(){
            LinearLayout talesList = findViewById(R.id.list);
            if (talesList != null){
                for(int i = 0; i < talesList.getChildCount(); i++){
                    ((ImageView)talesList.getChildAt(i).findViewById(R.id.play)).setImageResource(R.mipmap.tale_play);
                }
            }
        }

        private void updateRadioPlayBtnIcon(){
            ImageView radioPlayBtn = findViewById(R.id.playPause);
            if (radioPlayBtn != null){
                radioPlayBtn.setImageResource(R.mipmap.play);
            }
        }
    }
}
