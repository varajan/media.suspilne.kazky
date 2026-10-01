package media.suspilne.kazky.player;

import android.content.Context;
import android.media.AudioManager;

import media.suspilne.kazky.activities.MainActivity;

public class MediaVolume {
    AudioManager manager;
    Context context;

    public int getMaxLevel() {
        return manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public MediaVolume() {
        this.context = MainActivity.getActivity();
        manager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
    }

    public int getLevel() {
        return manager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void setLevel(int level) {
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
    }
}
