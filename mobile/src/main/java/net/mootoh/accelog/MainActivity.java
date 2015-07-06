package net.mootoh.accelog;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private boolean isTracking = false;
    SensorManager sensorManager;
    Sensor accelSensor;
    TextView textViewX, textViewY, textViewZ;
    private StringBuffer buffer;
    CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn = (Button)findViewById(R.id.toggleButton);
        btn.setText("Start");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMode((Button) v);
            }
        });

        textViewX = (TextView)findViewById(R.id.textViewX);
        textViewY = (TextView)findViewById(R.id.textViewY);
        textViewZ = (TextView)findViewById(R.id.textViewZ);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
//        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        canvasView = (CanvasView)findViewById(R.id.canvasView);
    }

    void writeBufferToFile() {
//        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "accelog.csv");
        File file = new File("/mnt/sdcard/accelog.csv");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.append(buffer.toString());
            osw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void toggleMode(final Button btn) {
        if (isTracking) {
            sensorManager.unregisterListener(this);
            btn.setText("Start");
            writeBufferToFile();
        } else {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
//            sensorManager.registerListener(this, accelSensor, (int)1e6);
            buffer = new StringBuffer();
            btn.setText("Stop");
        }

        isTracking = ! isTracking;
    }

    float cutLow(float value) {
        return (Math.abs(value) < 0.5f) ? 0.0f : value;
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float x = cutLow(event.values[0]);
        float y = cutLow(event.values[1]);
        float z = cutLow(event.values[2]);
        textViewX.setText("" + x);
        textViewY.setText("" + y);
        textViewZ.setText("" + z);

        Date now = new Date();
        buffer.append(now.toString() + "," + x + "," + y + "," + z + "\n");
        Log.d(TAG, now.toString() + "," + x + "," + y + "," + z);

        canvasView.setXYZ(x, y, z);
        canvasView.invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
