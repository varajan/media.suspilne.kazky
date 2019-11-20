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

    boolean matchesFilter(String filter){
        filter = filter.toLowerCase();
        return getName().toLowerCase().contains(filter) || getDescription().toLowerCase().contains(filter);
    }

    private View getView(){
        return ActivityReaders.getActivity().findViewById(R.id.readersList).findViewWithTag(getName());
    }

    void hide(){ getView().setVisibility(View.GONE); }

    void show(){ getView().setVisibility(View.VISIBLE); }

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
            ((TextView) readerView.findViewById(R.id.reader)).setText(name);
            ((TextView) readerView.findViewById(R.id.description)).setText(context.getString(R.string.reader_description, getDescription(), talesCount));
        }catch (Exception e){
            Log.e(SettingsHelper.application, e.getMessage());
            Log.e(SettingsHelper.application, e.getStackTrace().toString());
            e.printStackTrace();
        }
    }

    private int getPhoto(){
        switch (name){
            case R.string.andrii_hlyvniuk: return R.mipmap.andrii_hlyvniuk;
            case R.string.marko_galanevych: return R.mipmap.marko_galanevych;
            case R.string.alina_pash: return R.mipmap.alina_pash;
            case R.string.alyona_alyona: return R.mipmap.alyona_alyona;
            case R.string.vova_zi_lvova: return R.mipmap.vova_zi_lvova;
            case R.string.evgen_klopotenko: return R.mipmap.evgen_klopotenko;
            case R.string.evgen_maluha: return R.mipmap.evgen_maluha;
            case R.string.anna_nikitina: return R.mipmap.anna_nikitina;
            case R.string.vlad_fisun: return R.mipmap.vlad_fisun;
            case R.string.dmytro_schebetiuk: return R.mipmap.dmytro_schebetiiuk;
            case R.string.katia_rogova: return R.mipmap.katia_rogova;
            case R.string.michel_schur: return R.mipmap.michel_schur;
            case R.string.mariana_golovko: return R.mipmap.mariana_golovko;
            case R.string.marta_liubchyk: return R.mipmap.marta_liubchyk;
            case R.string.marusia_ionova: return R.mipmap.marusia_ionova;
            case R.string.oleksiy_dorychevsky: return R.mipmap.oleksiy_dorychevsky;
            case R.string.pavlo_varenitsa: return R.mipmap.pavlo_varenitsa;
            case R.string.roman_yasynovsky: return R.mipmap.roman_yasynovsky;
            case R.string.ruslana_khazipova: return R.mipmap.ruslana_khazipova;
            case R.string.sasha_koltsova: return R.mipmap.sasha_koltsova;
            case R.string.sergii_zhadan: return R.mipmap.sergii_zhadan;
            case R.string.sergii_kolos: return R.mipmap.sergii_kolos;
            case R.string.solomia_melnyk: return R.mipmap.solomia_melnyk;
            case R.string.stas_koroliov: return R.mipmap.stas_koroliov;
            case R.string.timur_miroshnychenko: return R.mipmap.timur_miroshnychenko;
            case R.string.hrystyna_soloviy: return R.mipmap.hrystyna_soloviy;
            case R.string.julia_jurina: return R.mipmap.julia_jurina;
            case R.string.jaroslav_ljudgin: return R.mipmap.jaroslav_ljudgin;

            default: return R.mipmap.logo;
        }
    }

    private int getTalesCount(){
        switch (name){
            case R.string.andrii_hlyvniuk: return 3;
            case R.string.marko_galanevych: return 2;
            case R.string.alina_pash: return 3;
            case R.string.alyona_alyona: return 3;
            case R.string.vova_zi_lvova: return 3;
            case R.string.evgen_klopotenko: return 3;
            case R.string.evgen_maluha: return 3;
            case R.string.anna_nikitina: return 3;
            case R.string.vlad_fisun: return 3;
            case R.string.dmytro_schebetiuk: return 3;
            case R.string.katia_rogova: return 3;
            case R.string.michel_schur: return 2;
            case R.string.mariana_golovko: return 3;
            case R.string.marta_liubchyk: return 3;
            case R.string.marusia_ionova: return 3;
            case R.string.oleksiy_dorychevsky: return 3;
            case R.string.pavlo_varenitsa: return 3;
            case R.string.roman_yasynovsky: return 3;
            case R.string.ruslana_khazipova: return 3;
            case R.string.sasha_koltsova: return 3;
            case R.string.sergii_zhadan: return 3;
            case R.string.sergii_kolos: return 3;
            case R.string.solomia_melnyk: return 4;
            case R.string.stas_koroliov: return 3;
            case R.string.timur_miroshnychenko: return 3;
            case R.string.hrystyna_soloviy: return 3;
            case R.string.julia_jurina: return 3;
            case R.string.jaroslav_ljudgin: return 3;

            default: return 0;
        }
    }
}
