package media.suspilne.kazky;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ActivitySettings extends ActivityMain {
    private Switch downloadAlltales;
    private Switch downloadFavoritetales;
    private Switch showOnlyFavorite;
    private Switch autoQuit;
    private SeekBar timeout;
    private TextView timeoutText;
    private Spinner languages;
    private int step = 5;
    private long totalRequiredSpace = 1400 * 1024 * 1024;
    private long hundred_kb  = 100 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);

        downloadAlltales = this.findViewById(R.id.downloadAlltales);
        downloadFavoritetales = this.findViewById(R.id.downloadFavoritetales);
        showOnlyFavorite = this.findViewById(R.id.showOnlyFavorite);
        autoQuit = this.findViewById(R.id.autoQuit);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);
        languages = this.findViewById(R.id.languages);

        setLanguages();
        setColorsAndState();

        showOnlyFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showOnlyFavorite", isChecked));
        autoQuit.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("autoQuit", isChecked));

        timeout.setOnSeekBarChangeListener(onTimeoutChange);
        languages.setOnItemSelectedListener(omLanguageSelect);
    }

    void setSwitch(String title, boolean isChecked){
        SettingsHelper.setBoolean(title, isChecked);
        setColorsAndState();
    }

    private void setLanguages(){
        ArrayList<Country> countries = new ArrayList<>();
        countries.add( new Country("en", getString(R.string.language_en), R.mipmap.uk));
        countries.add( new Country("uk", getString(R.string.language_ua), R.mipmap.ua));

        LanguageArrayAdapter arrayAdapter = new LanguageArrayAdapter(this, R.layout.language, countries);
        String currentLanguage = getResources().getConfiguration().locale.getLanguage();

        languages.setAdapter(arrayAdapter);

        for (int i = 0; i < countries.size(); i++) {
            if (countries.get(i).code.equals(currentLanguage)){
                languages.setSelection(i);
                break;
            }
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

        SettingsHelper.setBoolean("downloadAlltales", true);
        SettingsHelper.setBoolean("downloadFavoritetales", true);
        download();
        setColorsAndState();
    }

    private void doDownloadFavorite(){
        SettingsHelper.setBoolean("downloadFavoritetales", true);
        download();
        setColorsAndState();
    }

    private void doCleanup(boolean includeFavorite){
        for (TrackEntry track : new tales().gettales()) {
            if (includeFavorite || !track.isFavorite){
                track.deleteFile();
            }
        }

        SettingsHelper.setBoolean(includeFavorite ? "downloadFavoritetales" : "downloadAlltales", false);
        if (!includeFavorite && new tales().gettales(true).size() == 0){
            SettingsHelper.setBoolean("downloadFavoritetales", false);
        }

        setColorsAndState();
    }

    private CompoundButton.OnCheckedChangeListener onDownloadAllSelect = (buttonView, isChecked) -> {
        long usedSpace = SettingsHelper.usedSpace();
        String required = SettingsHelper.formattedSize(totalRequiredSpace - usedSpace);

        new AlertDialog.Builder(ActivitySettings.this)
            .setIcon(R.mipmap.icon_classic)
            .setTitle(isChecked ? R.string.download : R.string.clear)
            .setMessage(isChecked ? getString(R.string.downloadAlltalesQuestion, required) : getString(R.string.clearAlltalesQuestion))
            .setPositiveButton(isChecked ? R.string.download : R.string.clear, (dialog, which) -> {if (isChecked) doDownloadAll(); else doCleanup(false);})
            .setNegativeButton(R.string.no, (dialog, which) -> setColorsAndState())
            .setOnDismissListener(dialog -> setColorsAndState())
            .show();
    };

    private CompoundButton.OnCheckedChangeListener onDownloadFavoriteSelect = (buttonView, isChecked) ->
        new AlertDialog.Builder(ActivitySettings.this)
            .setIcon(R.mipmap.icon_classic)
            .setTitle(isChecked ? R.string.download : R.string.clear)
            .setMessage(isChecked ? R.string.downloadFavoritetalesQuestion : R.string.clearFavoritetalesQuestion)
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
        boolean isDownloadAlltales = SettingsHelper.getBoolean("downloadAlltales");
        boolean isDownloadFavoritetales = SettingsHelper.getBoolean("downloadFavoritetales");

        int primaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int primary = ContextCompat.getColor(this, R.color.colorPrimary);
        String usedSpace = getString(R.string.usedSpace, SettingsHelper.formattedSize(SettingsHelper.usedSpace()));
        String freeSpace = getString(R.string.freeSpace, SettingsHelper.formattedSize(SettingsHelper.freeSpace()));
        String minutes = SettingsHelper.getString("timeout", "5");

        timeoutText.setText(getString(R.string.x_minutes, minutes));
        timeout.setProgress(SettingsHelper.getInt("timeout", 1) / step);

        showOnlyFavorite.setChecked(isShowOnlyFavorite);
        autoQuit.setChecked(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        timeout.setEnabled(isAutoQuit);

        downloadAlltales.setOnCheckedChangeListener(null);
        downloadAlltales.setTextColor(isDownloadAlltales ? primaryDark : primary);
        downloadAlltales.setChecked(isDownloadAlltales);
        downloadAlltales.setOnCheckedChangeListener(onDownloadAllSelect);
        downloadAlltales.setText(getString(R.string.downloadAlltales) + (isDownloadAlltales && SettingsHelper.usedSpace() > hundred_kb ? usedSpace : freeSpace));

        downloadFavoritetales.setEnabled(!isDownloadAlltales);
        downloadFavoritetales.setOnCheckedChangeListener(null);
        downloadFavoritetales.setTextColor(isDownloadFavoritetales ? primaryDark : primary);
        downloadFavoritetales.setChecked(isDownloadAlltales || isDownloadFavoritetales);
        downloadFavoritetales.setOnCheckedChangeListener(onDownloadFavoriteSelect);
        downloadFavoritetales.setText(getString(R.string.downloadFavoritetales) + (!isDownloadAlltales && isDownloadFavoritetales && SettingsHelper.usedSpace() > hundred_kb ? usedSpace : ""));

        showOnlyFavorite.setTextColor(isShowOnlyFavorite ? primaryDark : primary);
        autoQuit.setTextColor(isAutoQuit ? primaryDark : primary);
        timeoutText.setTextColor(isAutoQuit ? primaryDark : primary);
    }

    AdapterView.OnItemSelectedListener omLanguageSelect = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String code = ((Country) languages.getSelectedItem()).code;
            String currentLanguage = LocaleManager.getLanguage();

            SettingsHelper.setString("Language", code);
            LocaleManager.setLanguage(ActivitySettings.this, code);

            if (!code.equals(currentLanguage)){
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

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