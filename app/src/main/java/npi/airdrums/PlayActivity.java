package npi.airdrums;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayActivity extends AppCompatActivity implements SensorEventListener {

    //Datos de sonidos
    private SoundManager soundPlayer;
    private ArrayList drums = new ArrayList();
    private int selectedDrum;

    //Datos de la brújula
    private float degree = 0;
    private float initialDegree = 800;
    private long lastUpdate = 0;
    private float drumSpace = 0;

    //Datos del acelerómetro
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private float[] gravityDirection = new float[3];

    //Datos de detección del golpeo
    private float lastSpeed = 0.0f;
    private static final int speedLimit = 60;
    private static final int speedLimitNegative = -60;
    private boolean hitting = false;
    private long startHitting = 0;
    private static final int hitTimeout = 100;

    //Datos de control de sensores
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor compass;

    //Datos de la vista
    TextView data;

    //Función onCreate, llamada al crear la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //Crea el reproductor de sonidos
        soundPlayer = new SoundManager(getApplicationContext());

        //Establece el stream de sonido
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //Lee los sonidos que figuran en res/raw
        drums.add(soundPlayer.load(R.raw.snare));
        drums.add(soundPlayer.load(R.raw.floortom));
        drums.add(soundPlayer.load(R.raw.crash));
        drums.add(soundPlayer.load(R.raw.midtom));

        selectedDrum = (int) drums.get(0);

        //Guarda el espacio entre los tambores en grados
        drumSpace = (360 / drums.size())+0.1f;

        //Establece los sensores que vamos a usar
        sensorManager = (SensorManager) getSystemService(getApplicationContext().SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_NORMAL);

        //Enlace con los elementos de la vista
        data = (TextView) findViewById(R.id.datoss);
    }

    //Función onResume, llamada cuando se vuelve de la pausa a la actividad
    @Override
    protected void onResume() {
        super.onResume();

        //Restablece el registro de datos de los sensores
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_NORMAL);
        initialDegree = 800;
    }

    //Funcion onPause, llamada cuando la actividad entra en pausa
    @Override
    protected void onPause() {
        super.onPause();

        //Deja de registrar los datos de los sensores
        sensorManager.unregisterListener(this);
    }

    //Función onSensorChanged, llamada cuando llega un dato del sensor
    @Override
    public void onSensorChanged(SensorEvent event) {

        //DATOS DE LA BRÚJULA
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {

            //Obtiene muestras cada 100 milisegundos
            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100 && !hitting) {
                lastUpdate = curTime;

                //Guarda los grados respecto al norte
                degree = Math.round(event.values[0]);

                //Establece el grado inicial
                if (initialDegree > 360) {

                    initialDegree = degree - (drumSpace/2);

                    if (initialDegree < 0)
                        
                        initialDegree = 360 + initialDegree;
                }

                //Guarda los grados respecto al grado inicial
                degree = degree - initialDegree;
                if (degree < 0)
                    degree = 360 + degree;

                //Selecciona el drum según la orientación
                selectDrum();
            }
        }

        //DATOS DEL ACELERÓMETRO
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

            //Obtiene el vector unitario de la gravedad
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


            //Detecta el movimiento de golpeo hacia abajo
            //Primero una aceleración negativa fuerte y despues una aceleración positiva fuerte.
            //Esto deve de darse dentro de un tiempo establecido.
            if (!hitting && lastSpeed < speedLimitNegative) {

                hitting = true;
                startHitting = System.currentTimeMillis();
            }
            else if (hitting && lastSpeed > speedLimit) {

                soundPlayer.play(selectedDrum);
                hitting = false;
            }
            else if (hitting) {

                long currentTime = System.currentTimeMillis();

                if ((currentTime - startHitting) > hitTimeout)

                    hitting = false;
            }

            lastSpeed = speed;

        }
    }

    //Función onAccuracyChanged, llamada cuando cambia la sensibilidad del sensor
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //Función para decidir que drum se está tocando ahora
    private void selectDrum() {

        selectedDrum = (int) drums.get((int)(degree/drumSpace));

        data.setText("Changed: " + ((int)(degree/drumSpace)));
    }

}
