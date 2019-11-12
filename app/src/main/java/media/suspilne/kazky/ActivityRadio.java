package media.suspilne.kazky;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

public class ActivityRadio extends ActivityMain {
    private ImageView playPauseBtn;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        unregisterReceiver();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        registerReceiver();
        setPlayBtnIcon();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        setPlayBtnIcon();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_radio);
        currentView = R.id.radio_menu;
        super.onCreate(savedInstanceState);
        registerReceiver();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ((ImageView) findViewById(R.id.onlinePlayer)).setImageResource(R.mipmap.online_player);

        playPauseBtn = findViewById(R.id.playPause);
        playPauseBtn.setImageResource(R.mipmap.play);
        playPauseBtn.setOnClickListener(v -> {
            if (isRadioPlaying()){
                stopPlayerService();
                setPlayBtnIcon();
            }else{
                stopPlayerService();

                if (isNetworkAvailable()){
                    Intent intent = new Intent(this, PlayerService.class);
                    intent.putExtra("type", getString(R.string.radio));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    }
                    else {
                        startService(intent);
                    }

                    setPlayBtnIcon();
                    this.setQuiteTimeout();
                }else{
                    showNoConnectionAlert();
              }
            }
        });
    }

    private boolean isRadioPlaying(){
        return isServiceRunning(PlayerService.class)
                && SettingsHelper.getString("StreamType").equals(getString(R.string.radio))
                && !SettingsHelper.getBoolean("playbackIsPaused");
    }

    private void showNoConnectionAlert(){
        new AlertDialog.Builder(ActivityRadio.this)
            .setIcon(R.mipmap.logo)
            .setTitle("Відсутній Інтернет!")
            .setMessage("Щоб схухати радіо потрібно підключення до Інтернета.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void setPlayBtnIcon(){
        boolean isPaused = SettingsHelper.getBoolean("playbackIsPaused");
        playPauseBtn.setImageResource(!isPaused && isRadioPlaying() ? R.mipmap.pause : R.mipmap.play);
    }

    private void registerReceiver(){
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(SettingsHelper.application);
            this.registerReceiver(receiver, filter);
        }catch (Exception e){ /*nothing*/ }
    }

    private void unregisterReceiver(){
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e){ /*nothing*/ }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("code")){
                case "SourceIsNotAccessible":
                    stopPlayerService();
                    setPlayBtnIcon();
                    showNoConnectionAlert();
                    break;

                case "SetPlayBtnIcon":
                    setPlayBtnIcon();
                    break;
            }
        }
    };
}