package media.suspilne.kazky;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;

@RequiresApi(api = Build.VERSION_CODES.M)
public class Settings extends MainActivity {
    private Switch batteryOptimization;
    private Switch talesDownload;
    private Switch talesPlayNext;
    private Switch autoQuit;
    private SeekBar timeout;
    private TextView timeoutText;
    private int step = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);

        batteryOptimization = this.findViewById(R.id.batteryOptimization);
        talesDownload = this.findViewById(R.id.talesDownload);
        talesPlayNext = this.findViewById(R.id.talesPlayNext);
        autoQuit = this.findViewById(R.id.autoQuit);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);

        setColorsAndState();
        setTaleReaders();
        askToContinueDownloadTales();

        talesPlayNext.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.setBoolean(Settings.this, "talesPlayNext", isChecked);
            setColorsAndState();
        });

        autoQuit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.setBoolean(Settings.this, "autoQuit", isChecked);
            setColorsAndState();
        });

        timeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SettingsHelper.setInt(Settings.this,"timeout", seekBar.getProgress() * step);
                timeoutText.setText(SettingsHelper.getString(Settings.this, "timeout", "0") + " хвилин");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setTaleReaders(){
        LinearLayout list = findViewById(R.id.list);
        ArrayList<Integer> ids = SettingsHelper.getTaleReaderIds(Settings.this);
        Collections.sort(ids);

        if (ids.size() == 0) ((TextView) findViewById(R.id.sectionTitle)).setText("");

        for (final int id:ids) {
            String readerName = SettingsHelper.getString(this, String.format("readerName-%d", id));
            Drawable readerPhoto = SettingsHelper.getImage(this, String.format("readerName-%d.jpg", id));
            View item = LayoutInflater.from(Settings.this).inflate(R.layout.reader, list, false);
            list.addView(item);

            TextView reader = item.findViewById(R.id.taleReader);
            ImageView photo = item.findViewById(R.id.photo);

            reader.setText(readerName);
            if (readerPhoto != null) photo.setImageDrawable(ImageHelper.getCircularDrawable(readerPhoto));
        }
    }

    private CompoundButton.OnCheckedChangeListener onIgnoreBatteryChangeListener = (buttonView, isChecked) -> {
        requestIgnoreBatteryOptimization();
        setColorsAndState();
    };

    private void download(){
        if (!this.isNetworkAvailable()){
            Toast.makeText(this, "Відсутній Інтернет!", Toast.LENGTH_LONG).show();
        } else {
            GetTaleIds download = new GetTaleIds();
            download.execute("https://kazky.suspilne.media/list", download.DOWNLOAD_ALL);
        }
    }

    private void doDownload(){
        download();
        SettingsHelper.setBoolean(Settings.this, "talesDownload", true);
        setColorsAndState();
    }

    private void doCleanup(){
        dropDownloads(".mp3");
        SettingsHelper.setBoolean(Settings.this, "talesDownload", false);
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
            .show();
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations(){
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        return pm.isIgnoringBatteryOptimizations(this.getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestIgnoreBatteryOptimization(){
        if (isIgnoringBatteryOptimizations()){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 0);
        }else{
            Uri packageUri = Uri.parse("package:" + this.getPackageName());
            startActivityForResult(new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setColorsAndState();
    }

    private void setColorsAndState() {
        boolean isTalesPlayNext = SettingsHelper.getBoolean(this, "talesPlayNext");
        boolean isAutoQuit = SettingsHelper.getBoolean(this, "autoQuit");
        boolean isTalesDownload = SettingsHelper.getBoolean(this, "talesDownload");
        int accent = ContextCompat.getColor(this, R.color.colorAccent);

        timeoutText.setText(SettingsHelper.getString(this, "timeout", "5") + " хвилин");
        timeout.setProgress(SettingsHelper.getInt(this, "timeout", 1) / step);

        talesPlayNext.setChecked(isTalesPlayNext);
        autoQuit.setChecked(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        timeout.setEnabled(isAutoQuit);

        talesDownload.setOnCheckedChangeListener(null);
        talesDownload.setTextColor(isTalesDownload ? accent : Color.GRAY);
        talesDownload.setChecked(isTalesDownload);
        talesDownload.setOnCheckedChangeListener(onDownloadTalesListener);

        if (Build.VERSION.SDK_INT > 23){
            batteryOptimization.setOnCheckedChangeListener(null);
            batteryOptimization.setTextColor(isIgnoringBatteryOptimizations() ? accent : Color.GRAY);
            batteryOptimization.setChecked(isIgnoringBatteryOptimizations());
            batteryOptimization.setOnCheckedChangeListener(onIgnoreBatteryChangeListener);
        }else{
            batteryOptimization.setVisibility(View.GONE);
        }

        talesPlayNext.setTextColor(isTalesPlayNext ? accent : Color.GRAY);
        autoQuit.setTextColor(isAutoQuit ? accent : Color.GRAY);
        timeoutText.setTextColor(isAutoQuit ? accent : Color.GRAY);
    }
}