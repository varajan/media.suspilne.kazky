package media.suspilne.kazky;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

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
        TextView infoText = findViewById(R.id.textView);

        infoText.setTextColor( SettingsHelper.getColor() );

        playPauseBtn = findViewById(R.id.playPause);
        playPauseBtn.setImageResource(R.mipmap.play);
        playPauseBtn.setOnClickListener(v -> {
            if (isRadioPlaying()){
                stopPlayerService();
                setPlayBtnIcon();
                stopVolumeReduceTimer();
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
                    this.setQuitTimeout();
                    this.setVolumeReduceTimer();
                }else{
                    showNoConnectionAlert();
                    stopVolumeReduceTimer();
                }
            }
        });
    }

    private boolean isRadioPlaying(){
        return isServiceRunning(PlayerService.class)
                && SettingsHelper.getString("StreamType").equals(getString(R.string.radio))
                && !Tales.isPaused();
    }

    private void showNoConnectionAlert(){
        new AlertDialog.Builder(ActivityRadio.this)
            .setIcon(R.mipmap.logo)
            .setTitle(R.string.an_error_occurred)
            .setMessage(R.string.radio_error)
            .setPositiveButton("OK", null)
            .show();
    }

    private void setPlayBtnIcon(){
        boolean isPaused = Tales.isPaused();
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