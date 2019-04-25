package media.suspilne.kazky;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class Tales extends AppCompatActivity {
    private void openSettingsView(){
        startActivityForResult(new Intent(Tales.this, Settings.class), 0);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                openSettingsView();
                return true;

            case KeyEvent.KEYCODE_BACK:
                System.exit(1);
                return true;
        }

        return super.onKeyDown(keycode, e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tales);

        this.findViewById(R.id.menuBtn).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) { openSettingsView(); }
            }
        );
    }
}
