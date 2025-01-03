package media.suspilne.kazky;

import android.Manifest;
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
                resetVolumeReduceTimer();
            }else{
                stopPlayerService();

                if (SettingsHelper.getBoolean("radioIsOff") || isNetworkUnavailable()){
                    showAlert(R.string.radio_error);
                    resetVolumeReduceTimer();
                    return;
                }

                if (!SettingsHelper.getBoolean("radioIsAvailable")){
                    showAlert(R.string.no_radio_text, "https://www.facebook.com/148182332275963/");
                    resetVolumeReduceTimer();
                    return;
                }

                if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                    requestPermission(Manifest.permission.POST_NOTIFICATIONS, R.string.no_post_notifications_permissions);
                    return;
                }

                Intent intent = new Intent(this, PlayerService.class);
                intent.putExtra("type", getString(R.string.radio));

                startForegroundService(intent);
                setPlayBtnIcon();
                this.resetQuitTimeout();
                this.resetVolumeReduceTimer();
            }
        });
    }

    private void showAlert(int message){
        new AlertDialog.Builder(ActivityRadio.this)
                .setIcon(R.mipmap.logo)
                .setTitle(R.string.an_error_occurred)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAlert(int message, String url){
        new AlertDialog.Builder(ActivityRadio.this)
                .setIcon(R.mipmap.logo)
                .setTitle(R.string.no_radio_header)
                .setMessage(message)
                .setPositiveButton(R.string.writeFB, (dialog, which) -> openFaceBookPage(url))
                .setNegativeButton(R.string.writeInsta, (dialog, which) -> openInstagramAccount())
                .setNeutralButton(R.string.cancel, null)
                .show();
    }

    private void setPlayBtnIcon(){
        boolean isPaused = Tales.isPaused();
        playPauseBtn.setImageResource(!isPaused && isRadioPlaying() ? R.mipmap.pause : R.mipmap.play);
        resetVolumeReduceTimer();
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
                    showAlert(R.string.radio_error);
                    break;

                case "SetPlayBtnIcon":
                    setPlayBtnIcon();
                    break;
            }
        }
    };
}