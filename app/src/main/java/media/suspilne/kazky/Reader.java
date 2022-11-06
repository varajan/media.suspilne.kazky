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
            Bitmap photo = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), this.photo, 100, 100);
            photo = this.photo.equals(R.mipmap.logo) ? photo : ImageHelper.getCircularDrawable(photo);
            int color = SettingsHelper.getColor();

            View readerView = getReaderView();
            TextView reader = readerView.findViewById(R.id.reader);
            TextView description = readerView.findViewById(R.id.description);

            ((ImageView)readerView.findViewById(R.id.photo)).setImageBitmap(photo);

            reader.setText(name);
            reader.setTextColor(color);

            description.setText(context.getString(R.string.reader_description, getDescription(), talesCount));
            description.setTextColor(color);
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
            case R.string.kateryna_ofliyan: return R.mipmap.kateryna_ofliyan;
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
            case R.string.jaroslav_lodygin: return R.mipmap.jaroslav_ljudgin;
            case R.string.ivan_marunych: return R.mipmap.ivan_marunych;
            case R.string.nata_smirnova: return R.mipmap.nata_smirnova;
            case R.string.oleg_moskalenko: return R.mipmap.oleg_moskalenko;
            case R.string.rosava: return R.mipmap.rosava;
            case R.string.jamala: return R.mipmap.jamala;
            case R.string.anastasiia_gudyma: return R.mipmap.anastasiia_gudyma;
            case R.string.dmytro_horkin: return R.mipmap.dmytro_horkin;
            case R.string.inna_grebeniuk: return R.mipmap.inna_grebeniuk;
            case R.string.olga_shurova: return R.mipmap.olga_shurova;
            case R.string.sergii_tanchynets: return R.mipmap.sergii_tanchynets;
            case R.string.ira_bova: return R.mipmap.ira_bova;
            case R.string.nina_matvienko: return R.mipmap.nina_matvienko;
            case R.string.olga_tokar: return R.mipmap.olga_tokar;
            case R.string.vitaliy_bilonozhko: return R.mipmap.vitaliy_bilonozhko;

            default: return R.mipmap.logo;
        }
    }

    private int getTalesCount(){
        return SettingsHelper.getInt(getName(), 0);
    }
}
