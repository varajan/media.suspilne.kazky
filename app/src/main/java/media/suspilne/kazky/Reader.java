package media.suspilne.kazky;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class Reader{
    public Integer name;
    public Integer description;
    public Integer photo;
    public Integer talesCount;

    public Reader(int name, int description){
        this.name = name;
        this.description = description;
        this.photo = getPhoto();
        this.talesCount = getTalesCount();
    }

    public String getName(){
        return ActivityMain.getActivity().getResources().getString(name);
    }
    public String getDescription(){
        return ActivityMain.getActivity().getResources().getString(description);
    }

    private View getReaderView(){
        return ActivityMain.getActivity().findViewById(R.id.readersList).findViewWithTag(getName());
    }

    public void setViewDetails(Context context){
        try
        {
            Bitmap reader = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), photo, 100, 100);
            reader = photo.equals(R.mipmap.logo) ? reader : ImageHelper.getCircularDrawable(reader);
            View readerView = getReaderView();

            readerView.findViewById(R.id.favorite).setVisibility(View.GONE);
            ((ImageView)readerView.findViewById(R.id.photo)).setImageBitmap(reader);
            ((TextView) readerView.findViewById(R.id.title)).setText(name);
            ((TextView) readerView.findViewById(R.id.reader)).setText(context.getString(R.string.reader_description, getDescription(), talesCount));
        }catch (Exception e){
            Log.e(SettingsHelper.application, e.getMessage());
            Log.e(SettingsHelper.application, e.getStackTrace().toString());
            e.printStackTrace();
        }
    }

    private int getPhoto(){
        switch (name){
            case R.string.andrii_hlyvniuk: return R.mipmap.andrii_hlyvniuk;
//            case R.string.marko_galanevych: return R.mipmap.marko_galanevych;

            default: return R.mipmap.logo;
        }
    }

    private int getTalesCount(){
        int result = 0;

        for(Tale tale:new Tales().getTales()){
            if (tale.getReader().equals(getName())) result++;
        }

        return result;
    }
}
