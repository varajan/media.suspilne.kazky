package media.suspilne.kazky;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ActivitySettings extends ActivityMain {
    private Switch fontColor;
    private Switch downloadAllTales;
    private Switch downloadFavoriteTales;
    private Switch showOnlyFavorite;
    private Switch autoQuit;
    private Switch volumeControl;
    private Switch parentLock;
    private Switch showBigImages;
    private SeekBar timeout;
    private SeekBar volumeTimeout;
    private int step = 5;
    private long totalRequiredSpace;
    private long hundred_kb  = 100 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);
        totalRequiredSpace = Integer.parseInt(getResources().getString(R.string.requiredSpace)) * 1024 * 1024;

        fontColor = this.findViewById(R.id.fontColor);
        downloadAllTales = this.findViewById(R.id.downloadAllTales);
        downloadFavoriteTales = this.findViewById(R.id.downloadFavoriteTales);
        showOnlyFavorite = this.findViewById(R.id.showOnlyFavorite);
        autoQuit = this.findViewById(R.id.autoQuit);
        timeout = this.findViewById(R.id.timeout);
        volumeControl = this.findViewById(R.id.volumeControl);
        volumeTimeout = this.findViewById(R.id.volumeControlTimeout);
        showBigImages = this.findViewById(R.id.showBigImages);
        parentLock = this.findViewById(R.id.parentLock);

        setColorsAndState();

        fontColor.setOnCheckedChangeListener((buttonView, isChecked) -> updateColor(isChecked));
        showOnlyFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showOnlyFavorite", isChecked));
        autoQuit.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("autoQuit", isChecked));
        volumeControl.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("volumeControl", isChecked));
        showBigImages.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showBigImages", isChecked));
        parentLock.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("parentLock", isChecked));
        timeout.setOnSeekBarChangeListener(onTimeoutChange);
        volumeTimeout.setOnSeekBarChangeListener(onVolumeTimeoutChange);

        if (SettingsHelper.getBoolean("parentLock")) applyParentLock();
    }

    int random(int min, int max) {
        return min + (int) (Math.random() * (max - min));
    }
    String questionAndAnswer(){
        int a = random(5, 10);
        int b = random(5, 10);
        int c = random(10, 30) - random(10, 20);
        int x = a*b+c;

        return c < 0
                ? "" + a + "x" + b + "" + c + "?:" + x
                : "" + a + "x" + b + "+" + c + "?:" + x;
    }

    void checkAccess(){
        if (!SettingsHelper.getBoolean("isParent")) { finish(); }
    }

    void applyParentLock(){
        SettingsHelper.setBoolean("isParent", false);

        String questionAndAnswer = questionAndAnswer();
        String question = questionAndAnswer.split(":")[0];
        String answer = questionAndAnswer.split(":")[1];

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);

        AlertDialog alert = new AlertDialog.Builder(ActivitySettings.this)
            .setIcon(R.mipmap.logo)
            .setTitle(question)
            .setView(input)
                .setPositiveButton(R.string.ok, (dialog, which) -> { SettingsHelper.setBoolean("isParent", input.getText().toString().equals(answer)); checkAccess(); })
                .setNegativeButton(R.string.prev, (dialog, which) -> checkAccess())
                .setOnDismissListener(dialog -> checkAccess() )
            .create();

        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alert.show();
    }

    void updateColor(boolean isChecked){
        if (isChecked){
            SettingsHelper.setBoolean("use.font.color", true);
            pickColor();
        } else{
            SettingsHelper.setBoolean("use.font.color", false);
            setColorsAndState();
        }
    }

    void pickColor(){
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .initialColor( SettingsHelper.getCustomColor() )
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(15)

            .setPositiveButton(R.string.ok, (dialog, selectedColor, allColors) ->
            {
                SettingsHelper.setColor(selectedColor);
                setColorsAndState();
            })

            .setNegativeButton(R.string.cancel, (dialog, which) ->
            {
                SettingsHelper.setBoolean("use.font.color", false);
                setColorsAndState();
            })
            .build()
            .show();
    }

    void setSwitch(String title, boolean isChecked){
        SettingsHelper.setBoolean(title, isChecked);
        setColorsAndState();

        if(title.equals("autoQuit") || title.equals("volumeControl")){
            resetQuitTimeout();
            resetVolumeReduceTimer();
        }
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
        boolean isFontColorOverridden = SettingsHelper.getBoolean("use.font.color");
        boolean isParentLock = SettingsHelper.getBoolean("parentLock");
        boolean isShowBigImages = SettingsHelper.getBoolean("showBigImages");

        int activeColor   = SettingsHelper.getColor();
        int inactiveColor = ContextCompat.getColor(this, R.color.gray);
        String usedSpace = getString(R.string.usedSpace, SettingsHelper.formattedSize(SettingsHelper.usedSpace()));
        String freeSpace = getString(R.string.freeSpace, SettingsHelper.formattedSize(SettingsHelper.freeSpace()));
        String quitMinutes = SettingsHelper.getString("timeout", "5");
        String volumeMinutes = SettingsHelper.getString("volumeMinutes", "5");

        timeout.setProgress(SettingsHelper.getInt("timeout", 1) / step);
        volumeTimeout.setProgress(SettingsHelper.getInt("volumeMinutes", 5));

        showOnlyFavorite.setChecked(isShowOnlyFavorite);
        autoQuit.setChecked(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        volumeControl.setChecked(isVolumeControl);
        volumeTimeout.setEnabled(isVolumeControl);

        fontColor.setChecked(isFontColorOverridden);
        fontColor.setTextColor(isFontColorOverridden ? activeColor : inactiveColor);

        downloadAllTales.setOnCheckedChangeListener(null);
        downloadAllTales.setTextColor(isDownloadAllTales ? activeColor : inactiveColor);
        downloadAllTales.setChecked(isDownloadAllTales);
        downloadAllTales.setOnCheckedChangeListener(onDownloadAllSelect);
        downloadAllTales.setText(getString(R.string.downloadAllTales) + (isDownloadAllTales && SettingsHelper.usedSpace() > hundred_kb ? usedSpace : freeSpace));

        downloadFavoriteTales.setEnabled(!isDownloadAllTales);
        downloadFavoriteTales.setOnCheckedChangeListener(null);
        downloadFavoriteTales.setTextColor(isDownloadFavoriteTales ? activeColor : inactiveColor);
        downloadFavoriteTales.setChecked(isDownloadAllTales || isDownloadFavoriteTales);
        downloadFavoriteTales.setOnCheckedChangeListener(onDownloadFavoriteSelect);
        downloadFavoriteTales.setText(getString(R.string.downloadFavoriteTales) + (!isDownloadAllTales && isDownloadFavoriteTales && SettingsHelper.usedSpace() > hundred_kb ? usedSpace : ""));

        showOnlyFavorite.setTextColor(isShowOnlyFavorite ? activeColor : inactiveColor);

        autoQuit.setTextColor(isAutoQuit ? activeColor : inactiveColor);
        autoQuit.setText(isAutoQuit ? getString(R.string.sleep_timeout_text, quitMinutes) : getString(R.string.sleep_timeout));

        volumeControl.setTextColor(isVolumeControl ? activeColor : inactiveColor);
        volumeControl.setText(isVolumeControl ? getString(R.string.volume_timeout_text, volumeMinutes) : getString(R.string.volume_timeout));

        showBigImages.setTextColor(isShowBigImages ? activeColor : inactiveColor);
        showBigImages.setChecked(isShowBigImages);

        parentLock.setTextColor(isParentLock ? activeColor : inactiveColor);
        parentLock.setChecked(isParentLock);
    }

    SeekBar.OnSeekBarChangeListener onTimeoutChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            SettingsHelper.setInt("timeout", seekBar.getProgress() * step);

            autoQuit.setText(getString(R.string.sleep_timeout_text, Integer.toString(seekBar.getProgress() * step )));

            resetQuitTimeout();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    SeekBar.OnSeekBarChangeListener onVolumeTimeoutChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            SettingsHelper.setInt("volumeMinutes", seekBar.getProgress());

            volumeControl.setText(getString(R.string.volume_timeout_text, Integer.toString(seekBar.getProgress())));

            resetVolumeReduceTimer();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
}