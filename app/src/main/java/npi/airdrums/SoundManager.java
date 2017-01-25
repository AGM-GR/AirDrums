package npi.airdrums;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {

    private Context activityContext;
    private SoundPool soundPool;
    private float rate = 1.0f;
    private float leftVolume = 1.0f;
    private float rightVolume = 1.0f;

    public SoundManager (Context activityContext) {

        soundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 100);

        this.activityContext = activityContext;

    }

    public int load (int idSonido) {

        return soundPool.load(activityContext, idSonido, 1);
    }

    public void play (int idSonido) {

        soundPool.play(idSonido,leftVolume,rightVolume,1,0,rate);
    }

}
