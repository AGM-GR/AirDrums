package npi.airdrums;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends AppCompatActivity {

    Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Asocia los elementos de la vista
        start = (Button) findViewById(R.id.start_button);

        //Define el comportamientos al pulsar un botón
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Inicia la siguiente acitividad.
                Intent i = new Intent(MainMenuActivity.this, PlayActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {


        super.onDestroy();
    }

}
