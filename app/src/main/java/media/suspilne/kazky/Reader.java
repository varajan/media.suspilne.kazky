package media.suspilne.kazky;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Reader {
    private int id;
    private String name;
    private String description;

    public Reader(int imgId, String name, String description){

    }

    public String getName() {return name; }
    public Integer talesCount;

    public void setViewDetails(Context context){
        try
        {
//            Bitmap author = HImages.getBitmapFromResource(ActivityBase.getActivity().getResources(), photo, 100, 100);
//            author = HImages.getCircularDrawable(author);
//            View composerView = getComposerView();
//
//            composerView.findViewById(R.id.favorite).setVisibility(View.GONE);
//            ((ImageView)composerView.findViewById(R.id.preview)).setImageBitmap(author);
//            ((TextView) composerView.findViewById(R.id.title)).setText(name);
//            ((TextView) composerView.findViewById(R.id.reader)).setText(context.getString(R.string.tales_count, talesCount));
        }catch (Exception e){
            Log.e(HSettings.application, e.getMessage());
            Log.e(HSettings.application, e.getStackTrace().toString());
            e.printStackTrace();
        }
    }
}
