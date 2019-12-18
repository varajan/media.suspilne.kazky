package media.suspilne.kazky;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ActivitySettings extends ActivityMain {
    private Switch downloadAllTales;
    private Switch downloadFavoriteTales;
    private Switch showOnlyFavorite;
    private Switch autoQuit;
    private Switch volumeControl;
    private SeekBar timeout;
    private TextView timeoutText;
    private int step = 5;
    private long totalRequiredSpace;
    private long hundred_kb  = 100 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);
        totalRequiredSpace = Integer.parseInt(getResources().getString(R.string.requiredSpace)) * 1024 * 1024;

        downloadAllTales = this.findViewById(R.id.downloadAllTales);
        downloadFavoriteTales = this.findViewById(R.id.downloadFavoriteTales);
        showOnlyFavorite = this.findViewById(R.id.showOnlyFavorite);
        autoQuit = this.findViewById(R.id.autoQuit);
        volumeControl = this.findViewById(R.id.volumeControl);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);

        setColorsAndState();

        showOnlyFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showOnlyFavorite", isChecked));
        autoQuit.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("autoQuit", isChecked));
        volumeControl.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("volumeControl", isChecked));
        timeout.setOnSeekBarChangeListener(onTimeoutChange);

//        pickColor(R.color.white);
    }

    void pickColor(int currentColor){
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .initialColor(currentColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(15)
            .setOnColorSelectedListener(selectedColor -> { Toast.makeText(getActivity(), "onColorSelected: 0x" + Integer.toHexString(selectedColor), Toast.LENGTH_LONG).show(); })
            .setPositiveButton("ok", (dialog, selectedColor, allColors) -> { /* update color*/ })
            .setNegativeButton("cancel", (dialog, which) -> {})
            .build()
            .show();
    }

    void setSwitch(String title, boolean isChecked){
        SettingsHelper.setBoolean(title, isChecked);
        setColorsAndState();
    }

    private void doDownloadAll(){
        long available = SettingsHelper.freeSpace();
        long usedSpace = SettingsHelper.usedSpace();
        long required = totalRequiredSpace - usedSpace;

        if (available < required){
            String title = getString(R.string.an_error_occurred);
            String message = getString(R.string.not_enough_space, SettingsHelper.formattedSize(available), SettingsHelper.formattedSize(required));

            showAlert(title, message);

            return;
        }

        SettingsHelper.setBoolean("downloadAllTales", true);
        SettingsHelper.setBoolean("downloadFavoriteTales", true);
        download();
        setColorsAndState();
    }

    private void doDownloadFavorite(){
        SettingsHelper.setBoolean("downloadFavoriteTales", true);
        download();
        setColorsAndState();
    }

    private void doCleanup(boolean includeFavorite){
        for (Tale tale : new Tales().getTales()) {
            if (includeFavorite || !tale.isFavorite){
                tale.deleteFile();
            }
        }

        SettingsHelper.setBoolean(includeFavorite ? "downloadFavoriteTales" : "downloadAllTales", false);
        if (!includeFavorite && new Tales().getTales(true).size() == 0){
            SettingsHelper.setBoolean("downloadFavoriteTales", false);
        }

        setColorsAndState();
    }

    private CompoundButton.OnCheckedChangeListener onDownloadAllSelect = (buttonView, isChecked) -> {
        long usedSpace = SettingsHelper.usedSpace();
        String required = SettingsHelper.formattedSize(totalRequiredSpace - usedSpace);

        new AlertDialog.Builder(ActivitySettings.this)
            .setIcon(R.mipmap.logo)
            .setTitle(isChecked ? R.string.download : R.string.clear)
            .setMessage(isChecked ? getString(R.string.downloadAllTalesQuestion, required) : getString(R.string.clearAllTalesQuestion))
            .setPositiveButton(isChecked ? R.string.download : R.string.clear, (dialog, which) -> {if (isChecked) doDownloadAll(); else doCleanup(false);})
            .setNegativeButton(R.string.no, (dialog, which) -> setColorsAndState())
            .setOnDismissListener(dialog -> setColorsAndState())
            .show();
    };

    private CompoundButton.OnCheckedChangeListener onDownloadFavoriteSelect = (buttonView, isChecked) ->
        new AlertDialog.Builder(ActivitySettings.this)
            .setIcon(R.mipmap.logo)
            .setTitle(isChecked ? R.string.download : R.string.clear)
            .setMessage(isChecked ? R.string.downloadFavoriteTalesQuestion : R.string.clearFavoriteTalesQuestion)
            .setPositiveButton(isChecked ? R.string.download : R.string.clear, (dialog, which) -> {if (isChecked) doDownloadFavorite(); else doCleanup(true);})
            .setNegativeButton(R.string.no, (dialog, which) -> setColorsAndState())
            .setOnDismissListener(dialog -> setColorsAndState())
            .show();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setColorsAndState();
    }

    private void setColorsAndState() {
        boolean isShowOnlyFavorite = SettingsHelper.getBoolean("showOnlyFavorite");
        boolean isAutoQuit = SettingsHelper.getBoolean("autoQuit");
        boolean isDownloadAllTales = SettingsHelper.getBoolean("downloadAllTales");
        boolean isDownloadFavoriteTales = SettingsHelper.getBoolean("downloadFavoriteTales");
        boolean isVolumeControl = SettingsHelper.getBoolean("volumeControl");

        int primaryDark = ContextCompat.getColor(this, R.color.colorAccent);
        int primary = ContextCompat.getColor(this, R.color.superLight);
        String usedSpace = getString(R.string.usedSpace, SettingsHelper.formattedSize(SettingsHelper.usedSpace()));
        String freeSpace = getString(R.string.freeSpace, SettingsHelper.formattedSize(SettingsHelper.freeSpace()));
        String minutes = SettingsHelper.getString("timeout", "5");

        timeoutText.setText(getString(R.string.x_minutes, minutes));
        timeout.setProgress(SettingsHelper.getInt("timeout", 1) / step);

        showOnlyFavorite.setChecked(isShowOnlyFavorite);
        autoQuit.setChecked(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        volumeControl.setEnabled(isAutoQuit);
        volumeControl.setChecked(isAutoQuit && isVolumeControl);

        downloadAllTales.setOnCheckedChangeListener(null);
        downloadAllTales.setTextColor(isDownloadAllTales ? primaryDark : primary);
        downloadAllTales.setChecked(isDownloadAllTales);
        downloadAllTales.setOnCheckedChangeListener(onDownloadAllSelect);
        downloadAllTales.setText(getString(R.string.downloadAllTales) + (isDownloadAllTales && SettingsHelper.usedSpace() > hundred_kb ? usedSpace : freeSpace));

        downloadFavoriteTales.setEnabled(!isDownloadAllTales);
        downloadFavoriteTales.setOnCheckedChangeListener(null);
        downloadFavoriteTales.setTextColor(isDownloadFavoriteTales ? primaryDark : primary);
        downloadFavoriteTales.setChecked(isDownloadAllTales || isDownloadFavoriteTales);
        downloadFavoriteTales.setOnCheckedChangeListener(onDownloadFavoriteSelect);
        downloadFavoriteTales.setText(getString(R.string.downloadFavoriteTales) + (!isDownloadAllTales && isDownloadFavoriteTales && SettingsHelper.usedSpace() > hundred_kb ? usedSpace : ""));

        showOnlyFavorite.setTextColor(isShowOnlyFavorite ? primaryDark : primary);
        autoQuit.setTextColor(isAutoQuit ? primaryDark : primary);
        timeoutText.setTextColor(isAutoQuit ? primaryDark : primary);
        volumeControl.setTextColor(isAutoQuit && isVolumeControl ? primaryDark : primary);
    }

    SeekBar.OnSeekBarChangeListener onTimeoutChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            SettingsHelper.setInt("timeout", seekBar.getProgress() * step);
            String minutes = SettingsHelper.getString("timeout", "0");

            timeoutText.setText(getString(R.string.x_minutes, minutes));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}