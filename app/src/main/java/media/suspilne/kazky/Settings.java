package media.suspilne.kazky;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class Settings extends AppCompatActivity {
    private Switch autoStart;
    private Switch autoStop;
    private SeekBar timeout;
    private TextView timeoutText;
    private int step = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        autoStart = this.findViewById(R.id.autoStart);
        autoStop = this.findViewById(R.id.autoStop);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);

        setColorsAndState();

        this.findViewById(R.id.backBtn).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        );

        autoStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsHelper.setBoolean(Settings.this, "autoStart", isChecked);
                setColorsAndState();
            }
        });

        autoStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsHelper.setBoolean(Settings.this, "autoStop", isChecked);
                setColorsAndState();
            }
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

    private void setColorsAndState() {
        boolean isAutoStart = SettingsHelper.getBoolean(this, "autoStart");
        boolean isAutoStop = SettingsHelper.getBoolean(this, "autoStop");

        timeoutText.setText(SettingsHelper.getString(this, "timeout", "5") + " хвилин");
        timeout.setProgress(SettingsHelper.getInt(this, "timeout", 1) / step);

        autoStart.setChecked(isAutoStart);
        autoStop.setChecked(isAutoStop);
        timeout.setEnabled(isAutoStop);
        timeout.setEnabled(isAutoStop);

        autoStart.setTextColor(isAutoStart ? Color.RED : Color.GRAY);
        autoStop.setTextColor(isAutoStop ? Color.RED : Color.GRAY);
        timeoutText.setTextColor(isAutoStop ? Color.RED : Color.GRAY);
    }
}
