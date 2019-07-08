package media.suspilne.kazky;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

public class ActivityInfo extends ActivityBase {
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_info);
        currentView = R.id.info_menu;
        super.onCreate(savedInstanceState);

        setTaleReaders();
        String version = HSettings.getVersionName(this);
        ((TextView)findViewById(R.id.infoText)).setText(getString(R.string.description, version));
    }

    private void setTaleReaders(){
        LinearLayout list = findViewById(R.id.list);
        ArrayList<Integer> ids = HSettings.getTaleReaderIds(this);
        Collections.sort(ids);

        if (ids.size() == 0) findViewById(R.id.sectionTitle).setVisibility(View.GONE);

        for (final int id:ids) {
            String readerName = HSettings.getString(String.format("readerName-%d", id));
            Drawable readerPhoto = HSettings.getImage(String.format("readerName-%d.jpg", id));
            View item = LayoutInflater.from(this).inflate(R.layout.reader, list, false);
            list.addView(item);

            TextView reader = item.findViewById(R.id.taleReader);
            ImageView photo = item.findViewById(R.id.photo);

            reader.setText(readerName);
            if (readerPhoto != null) photo.setImageDrawable(HImages.getCircularDrawable(readerPhoto));
        }
    }
}
