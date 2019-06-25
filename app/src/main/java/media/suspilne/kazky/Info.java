package media.suspilne.kazky;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Info extends MainActivity {
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_info);
        currentView = R.id.info_menu;
        super.onCreate(savedInstanceState);

        ((TextView)findViewById(R.id.infoText)).setText(R.string.description);
        findViewById(R.id.fiveStars).setOnClickListener(rateApp);
    }

    View.OnClickListener rateApp = view -> {

    };
}
