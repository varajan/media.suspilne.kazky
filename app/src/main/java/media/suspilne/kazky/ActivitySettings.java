package media.suspilne.kazky;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.Arrays;
import java.util.List;

public class ActivitySettings extends ActivityMain {
    private Switch fontColor;
    private Switch downloadAllTales;
    private Switch downloadFavoriteTales;
    private Switch autoQuit;
    private Switch volumeControl;
    private Switch parentLock;

    private Switch showBigImages;
    private Switch showBabiesTales;
    private Switch showKidsTales;
    private Switch showLullabies;
    private Switch showOnlyFavorite;
    private RadioGroup sorting;
    private Switch groupByReader;
    private Switch skipIntro;

    private SeekBar timeout;
    private SeekBar volumeTimeout;
    private final int step = 5;
    private long totalRequiredSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);
        totalRequiredSpace = (long) Integer.parseInt(getResources().getString(R.string.requiredSpace)) * 1024 * 1024;

        fontColor = this.findViewById(R.id.fontColor);
        downloadAllTales = this.findViewById(R.id.downloadAllTales);
        downloadFavoriteTales = this.findViewById(R.id.downloadFavoriteTales);
        parentLock = this.findViewById(R.id.parentLock);
        autoQuit = this.findViewById(R.id.autoQuit);
        timeout = this.findViewById(R.id.timeout);
        volumeControl = this.findViewById(R.id.volumeControl);
        volumeTimeout = this.findViewById(R.id.volumeControlTimeout);

        showBabiesTales = this.findViewById(R.id.showBabiesTales);
        showKidsTales = this.findViewById(R.id.showKidsTales);
        showBigImages = this.findViewById(R.id.showBigImages);
        showLullabies = this.findViewById(R.id.showLullabies);
        showOnlyFavorite = this.findViewById(R.id.showOnlyFavorite);
        sorting = this.findViewById(R.id.sorting);
        groupByReader = this.findViewById(R.id.groupByReader);
        skipIntro = this.findViewById(R.id.skipIntro);

        setColorsAndState();
        setSorting();

        fontColor.setOnCheckedChangeListener((buttonView, isChecked) -> updateColor(isChecked));
        autoQuit.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("autoQuit", isChecked));
        volumeControl.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("volumeControl", isChecked));
        parentLock.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("parentLock", isChecked));
        timeout.setOnSeekBarChangeListener(onTimeoutChange);
        volumeTimeout.setOnSeekBarChangeListener(onVolumeTimeoutChange);

        showBigImages.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showBigImages", isChecked));
        showBabiesTales.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showBabiesTales", isChecked));
        showKidsTales.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showKidsTales", isChecked));
        showLullabies.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showLullabies", isChecked));
        showOnlyFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showOnlyFavorite", isChecked));
        groupByReader.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("groupByReader", isChecked));
        skipIntro.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("skipIntro", isChecked));

        if (SettingsHelper.getBoolean("parentLock")) applyParentLock();

        if (!hasPermission(android.Manifest.permission.POST_NOTIFICATIONS)) {
            requestPermission(Manifest.permission.POST_NOTIFICATIONS, R.string.no_post_notifications_permissions_title, R.string.no_post_notifications_permissions_error);
        }
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

    private void setSwitch(String title, boolean isChecked){
        List<String> filterOptions = Arrays.asList("showBabiesTales", "showKidsTales", "showLullabies", "showOnlyFavorite");
        if (filterOptions.contains(title)) { Tales.setTalesCountUpdated(false); }

        SettingsHelper.setBoolean(title, isChecked);
        if (title.equals("groupByReader")) new Tales().setTalesList();

        if (!Tales.getShowForBabies() && !Tales.getShowForKids() && !Tales.getShowLullabies()){
            Tales.setShowForBabies(true);
            Tales.setShowForKids(true);
            Tales.setShowLullabies(true);
        }

        setColorsAndState();

        if(title.equals("autoQuit") || title.equals("volumeControl")){
            resetQuitTimeout();
            resetVolumeReduceTimer();
        }
    }

    private void setSorting() {
        switch (sorting.getCheckedRadioButtonId()){
            case R.id.shuffle:
                SettingsHelper.setString("sorting", "shuffle");
                groupByReader.setVisibility(View.GONE);
                break;

            case R.id.sortAsc:
                SettingsHelper.setString("sorting", "sortAsc");
                groupByReader.setVisibility(View.VISIBLE);
                break;

            case R.id.sort19:
                SettingsHelper.setString("sorting", "sort19");
                groupByReader.setVisibility(View.GONE);
                break;

            case R.id.sort91:
                SettingsHelper.setString("sorting", "sort91");
                groupByReader.setVisibility(View.GONE);
                break;
        }

        setColorsAndState();
        new Tales().setTalesList();
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
        for (Tale tale : new Tales().items) {
            if (includeFavorite || !tale.isFavorite){
                tale.deleteFile();
            }
        }

        SettingsHelper.setBoolean(includeFavorite ? "downloadFavoriteTales" : "downloadAllTales", false);
        if (!includeFavorite && new Tales().getFavoriteCount() == 0){
            SettingsHelper.setBoolean("downloadFavoriteTales", false);
        }

        setColorsAndState();
    }

    private final CompoundButton.OnCheckedChangeListener onDownloadAllSelect = (buttonView, isChecked) -> {
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

    private final CompoundButton.OnCheckedChangeListener onDownloadFavoriteSelect = (buttonView, isChecked) ->
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
        boolean isShowKidsTales = SettingsHelper.getBoolean("showKidsTales");
        boolean isShowBabiesTales = SettingsHelper.getBoolean("showBabiesTales");
        boolean isShowLullabies = SettingsHelper.getBoolean("showLullabies");
        boolean isShowBigImages = SettingsHelper.getBoolean("showBigImages");
        boolean isGroupByReader = SettingsHelper.getBoolean("groupByReader");
        boolean isShuffle = SettingsHelper.getBoolean("shuffle");
        boolean isSkipIntro = SettingsHelper.getBoolean("skipIntro");

        int activeColor   = SettingsHelper.getColor();
        int inactiveColor = ContextCompat.getColor(this, R.color.gray);
        String usedSpace = getString(R.string.usedSpace, SettingsHelper.formattedSize(SettingsHelper.usedSpace()));
        String freeSpace = getString(R.string.freeSpace, SettingsHelper.formattedSize(SettingsHelper.freeSpace()));
        String quitMinutes = SettingsHelper.getString("timeout", "5");
        String volumeMinutes = SettingsHelper.getString("volumeMinutes", "5");

        timeout.setProgress(SettingsHelper.getInt("timeout", 1) / step);
        volumeTimeout.setProgress(SettingsHelper.getInt("volumeMinutes", 5));

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
        long hundred_kb = 100 * 1024;
        downloadAllTales.setText(getString(R.string.downloadAllTales) + (isDownloadAllTales && SettingsHelper.usedSpace() > hundred_kb ? usedSpace : freeSpace));

        downloadFavoriteTales.setEnabled(!isDownloadAllTales);
        downloadFavoriteTales.setOnCheckedChangeListener(null);
        downloadFavoriteTales.setTextColor(isDownloadFavoriteTales ? activeColor : inactiveColor);
        downloadFavoriteTales.setChecked(isDownloadAllTales || isDownloadFavoriteTales);
        downloadFavoriteTales.setOnCheckedChangeListener(onDownloadFavoriteSelect);
        downloadFavoriteTales.setText(getString(R.string.downloadFavoriteTales) + (!isDownloadAllTales && isDownloadFavoriteTales && SettingsHelper.usedSpace() > hundred_kb ? usedSpace : ""));

        autoQuit.setTextColor(isAutoQuit ? activeColor : inactiveColor);
        autoQuit.setText(isAutoQuit ? getString(R.string.sleep_timeout_text, quitMinutes) : getString(R.string.sleep_timeout));

        volumeControl.setTextColor(isVolumeControl ? activeColor : inactiveColor);
        volumeControl.setText(isVolumeControl ? getString(R.string.volume_timeout_text, volumeMinutes) : getString(R.string.volume_timeout));

        showOnlyFavorite.setChecked(isShowOnlyFavorite);
        showOnlyFavorite.setTextColor(isShowOnlyFavorite ? activeColor : inactiveColor);

        showLullabies.setChecked(isShowLullabies);
        showLullabies.setTextColor(isShowLullabies ? activeColor : inactiveColor);

        showKidsTales.setChecked(isShowKidsTales);
        showKidsTales.setTextColor(isShowKidsTales ? activeColor : inactiveColor);

        showBabiesTales.setChecked(isShowBabiesTales);
        showBabiesTales.setTextColor(isShowBabiesTales ? activeColor : inactiveColor);

        showBigImages.setChecked(isShowBigImages);
        showBigImages.setTextColor(isShowBigImages ? activeColor : inactiveColor);

        groupByReader.setChecked(isGroupByReader);
        groupByReader.setTextColor(isGroupByReader ? activeColor : inactiveColor);
        groupByReader.setEnabled(!isShuffle);

        skipIntro.setChecked(isSkipIntro);
        skipIntro.setTextColor(isSkipIntro ? activeColor : inactiveColor);

        parentLock.setTextColor(isParentLock ? activeColor : inactiveColor);
        parentLock.setChecked(isParentLock);

        setSortingState();
    }

    private void setSortingState(){
        int activeColor   = SettingsHelper.getColor();
        int inactiveColor = ContextCompat.getColor(this, R.color.gray);

        ((RadioButton)findViewById(R.id.shuffle)).setTextColor(inactiveColor);
        ((RadioButton)findViewById(R.id.sortAsc)).setTextColor(inactiveColor);
        ((RadioButton)findViewById(R.id.sort19)).setTextColor(inactiveColor);
        ((RadioButton)findViewById(R.id.sort91)).setTextColor(inactiveColor);

        sorting.setOnCheckedChangeListener(null);

        switch (SettingsHelper.getString("sorting")){
            case "sort19":
                ((RadioButton)findViewById(R.id.sort19)).setTextColor(activeColor);
                sorting.check(R.id.sort19);
                break;

            case "sort91":
                ((RadioButton)findViewById(R.id.sort91)).setTextColor(activeColor);
                sorting.check(R.id.sort91);
                break;

            case "sortAsc":
                ((RadioButton)findViewById(R.id.sortAsc)).setTextColor(activeColor);
                sorting.check(R.id.sortAsc);
                break;

            default:
                ((RadioButton)findViewById(R.id.shuffle)).setTextColor(activeColor);
                sorting.check(R.id.shuffle);
                break;
        }

        sorting.setOnCheckedChangeListener((x, y) -> setSorting());
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