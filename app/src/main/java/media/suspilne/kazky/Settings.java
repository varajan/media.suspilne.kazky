package media.suspilne.kazky;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends MainActivity {
    private Switch talesDownload;
    private Switch autoQuit;
    private SeekBar timeout;
    private TextView timeoutText;
    private int step = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);

        talesDownload = this.findViewById(R.id.talesDownload);
        autoQuit = this.findViewById(R.id.autoQuit);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);

        setColorsAndState();
        askToContinueDownloadTales();

        autoQuit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.setBoolean("autoQuit", isChecked);
            setColorsAndState();
        });

        timeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SettingsHelper.setInt("timeout", seekBar.getProgress() * step);
                timeoutText.setText(SettingsHelper.getString("timeout", "0") + " хвилин");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void download(){
        if (!SettingsHelper.isNetworkAvailable()){
            Toast.makeText(this, "Відсутній Інтернет!", Toast.LENGTH_LONG).show();
        } else {
            GetTaleIds download = new GetTaleIds();
            download.execute("https://kazky.suspilne.media/list", download.DOWNLOAD_ALL);
        }
    }

    private void doDownload(){
        download();
        SettingsHelper.setBoolean("talesDownload", true);
        setColorsAndState();
    }

    private void doCleanup(){
        dropDownloads(".mp3");
        SettingsHelper.setBoolean("talesDownload", false);
        setColorsAndState();
    }

    private CompoundButton.OnCheckedChangeListener onDownloadTalesListener = (buttonView, isChecked) -> {
        String yes = isChecked ? "Завантажити" : "Видалити";
        String title = isChecked ? "Завантажити казки у пристрій?" : "Видалити казки із пристрою?";
        String message = isChecked
                ? "Це займе приблизно 130MB. Але потім казки можна слухати без Інтернета."
                : "Ви не зможете слухати казки без Інтерета.";

        new AlertDialog.Builder(Settings.this)
            .setIcon(R.mipmap.logo)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(yes, (dialog, which) -> {if (isChecked) doDownload(); else doCleanup();})
            .setNegativeButton("Ні", (dialog, which) -> setColorsAndState())
            .setOnDismissListener(dialog -> setColorsAndState())
            .show();
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setColorsAndState();
    }

    private void setColorsAndState() {
        boolean isAutoQuit = SettingsHelper.getBoolean("autoQuit");
        boolean isTalesDownload = SettingsHelper.getBoolean("talesDownload");
        int accent = ContextCompat.getColor(this, R.color.colorAccent);

        timeoutText.setText(SettingsHelper.getString("timeout", "5") + " хвилин");
        timeout.setProgress(SettingsHelper.getInt("timeout", 1) / step);

        autoQuit.setChecked(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        timeout.setEnabled(isAutoQuit);

        talesDownload.setOnCheckedChangeListener(null);
        talesDownload.setTextColor(isTalesDownload ? accent : Color.GRAY);
        talesDownload.setChecked(isTalesDownload);
        talesDownload.setOnCheckedChangeListener(onDownloadTalesListener);

        autoQuit.setTextColor(isAutoQuit ? accent : Color.GRAY);
        timeoutText.setTextColor(isAutoQuit ? accent : Color.GRAY);
    }
}