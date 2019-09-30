package media.suspilne.kazky;

import android.os.Bundle;
import android.widget.TextView;

public class ActivityInfo extends ActivityBase {
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_info);
        currentView = R.id.info_menu;
        super.onCreate(savedInstanceState);

        String version = HSettings.getVersionName(this);
        ((TextView)findViewById(R.id.infoText)).setText(getString(R.string.description, version));
    }
}