package media.suspilne.kazky;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class Settings extends AppCompatActivity {
    private Switch autoStart;
    private Switch autoStop;
    private SeekBar timeout;
    private TextView timeoutText;
    private ImageView radioButton;
    private int step = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        autoStart = this.findViewById(R.id.autoStart);
        autoStop = this.findViewById(R.id.autoStop);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);
        radioButton = this.findViewById(R.id.radioBtn);

        autoStart.setChecked(SettingsHelper.getBoolean(this, "autoStart"));
        autoStop.setChecked(SettingsHelper.getBoolean(this, "autoStop"));
        timeoutText.setText(SettingsHelper.getString(this, "timeout", "30") + " хвилин");
        timeout.setProgress(SettingsHelper.getInt(this, "timeout", 6) / step);
        timeout.setEnabled(SettingsHelper.getBoolean(this, "autoStop"));
        timeout.setEnabled(SettingsHelper.getBoolean(this, "autoStop"));

        radioButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { finish(); }
                }
        );

        autoStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsHelper.setBoolean(Settings.this, "autoStart", isChecked);
            }
        });

        autoStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsHelper.setBoolean(Settings.this, "autoStop", isChecked);
                timeout.setEnabled(isChecked);
            }
        });

        timeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SettingsHelper.setInt(Settings.this,"timeout", seekBar.getProgress() * step);
                timeoutText.setText(SettingsHelper.getString(Settings.this, "timeout", "0") + " хвилин");
            }
        });
    }
}
