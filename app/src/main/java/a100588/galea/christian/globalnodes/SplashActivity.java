package a100588.galea.christian.globalnodes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Chris on 18/05/2017.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("SPLISH","SPLOSH");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}