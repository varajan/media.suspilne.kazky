package media.suspilne.kazky;

import android.os.Bundle;
import android.widget.TextView;

public class ActivityInfo extends ActivityMain {
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_info);
        currentView = R.id.info_menu;
        super.onCreate(savedInstanceState);

        TextView infoText = findViewById(R.id.infoText);

        infoText.setText( getString(R.string.description, SettingsHelper.getVersionName()) );
        infoText.setTextColor( SettingsHelper.getColor() );
    }
}