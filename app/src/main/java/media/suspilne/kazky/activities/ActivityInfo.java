package media.suspilne.kazky.activities;

import android.os.Bundle;
import android.widget.TextView;

import media.suspilne.kazky.R;
import media.suspilne.kazky.helpers.SettingsHelper;

public class ActivityInfo extends MainActivity {
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_info);
        currentView = R.id.info_menu;
        super.onCreate(savedInstanceState);

        TextView infoText = findViewById(R.id.infoText);

        infoText.setText( getString(R.string.description, SettingsHelper.getVersionName()) );
        infoText.setTextColor( SettingsHelper.getColor() );
    }
}