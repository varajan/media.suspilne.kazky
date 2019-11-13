package media.suspilne.kazky;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class Reader{
    public Integer name;
    public Integer photo;
    public Integer talesCount;

    public Reader(int name){
        this.name = name;
        this.photo = getPhoto();
        this.talesCount = getTalesCount();
    }

    public String getName(){
        return ActivityMain.getActivity().getResources().getString(name);
    }

    private View getReaderView(){
        return ActivityMain.getActivity().findViewById(R.id.readersList).findViewWithTag(getName());
    }

    public void setViewDetails(Context context){
        try
        {
            Bitmap author = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), photo, 100, 100);
            author = ImageHelper.getCircularDrawable(author);
            View readerView = getReaderView();

            readerView.findViewById(R.id.favorite).setVisibility(View.GONE);
            ((ImageView)readerView.findViewById(R.id.photo)).setImageBitmap(author);
            ((TextView) readerView.findViewById(R.id.title)).setText(name);
            ((TextView) readerView.findViewById(R.id.reader)).setText(context.getString(R.string.reader_tales, talesCount));
        }catch (Exception e){
            Log.e(SettingsHelper.application, e.getMessage());
            Log.e(SettingsHelper.application, e.getStackTrace().toString());
            e.printStackTrace();
        }
    }

    private int getPhoto(){
        switch (name){
//            case R.string.donizetti: return R.mipmap.donizetti;
//            case R.string.gounod: return R.mipmap.gounod;

            default: return 0;
        }
    }

    private int getTalesCount(){
        switch (name){
//            case R.string.vivaldi: return 4;
//            case R.string.wagner: return 2;

            default: return 0;
        }
    }
}
