package npi.airdrums;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PlayActivity extends AppCompatActivity implements SensorEventListener {

    private SoundManager reproductor;
    private int snare;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private float[] gravityDirection = new float[3];
    private float lastSpeed = (float) 0.0;
    private long lastUpdate = 0;
    private static final int speedLimit = 90;
    private static final int speedLimitNegative = -90;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //Crea el reproductor de sonidos
        reproductor = new SoundManager(getApplicationContext());

        //Establece el stream de sonido
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //Lee los sonidos que figuran en res/raw
        snare = reproductor.load(R.raw.snare);

        //Establece los sensores que vamos a usar
        sensorManager = (SensorManager) getSystemService(getApplicationContext().SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            final float alpha = 0.8f;

            //Calcula la gravedad con los datos del acelerómetro
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            //Calcula la aceleración restandole la gravedad
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            //Obtiene muestras cada x milisegundos
            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 60) {
                lastUpdate = curTime;

                //Obtengo el vector unitario de la gravedad
                float modulo = (float) Math.sqrt((gravity[0]*gravity[0])+(gravity[1]*gravity[1])+(gravity[2]*gravity[2]));
                gravityDirection[0] = gravity[0] / modulo;
                gravityDirection[1] = gravity[1] / modulo;
                gravityDirection[2] = gravity[2] / modulo;

                //Elimina los valores basura de la dirección
                for (int i=0; i<gravityDirection.length; i++)
                    if (gravityDirection[i] < 0.5f && gravityDirection[i] > -0.5f)
                        gravityDirection[i] = 0;

                //Calcula la velocidad en proporción de la dirección de la gravedad
                float speed = ((linear_acceleration[0]*gravityDirection[0]) + (linear_acceleration[1]*gravityDirection[1]) + (linear_acceleration[2]*gravityDirection[2])) * 10;

                if (speed < lastSpeed*0.65 && lastSpeed > speedLimit) {

                    reproductor.play(snare);
                }

                lastSpeed = speed;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
