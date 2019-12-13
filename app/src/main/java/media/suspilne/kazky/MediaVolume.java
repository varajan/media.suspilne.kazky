package media.suspilne.kazky;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

import static media.suspilne.kazky.SettingsHelper.application;

public class MediaVolume {
    AudioManager manager;
    Context context;

    public MediaVolume(Context context) {
        this.context = context;
        manager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
    }

    public MediaVolume() {
        this.context = ActivityMain.getActivity();
        manager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
    }

    public int getLevel(){
        return manager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public int getMaxLevel(){
        return manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void setLevel(int level){
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
    }

    public void restoreLevel(){
        String level = this.context.getSharedPreferences(application,0).getString("STREAM_MUSIC_LEVEL", "0");

        setLevel(Integer.parseInt(level));
    }

    public void saveLevel(){
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(application, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("STREAM_MUSIC_LEVEL", String.valueOf(getLevel()));
        editor.apply();
    }
}
