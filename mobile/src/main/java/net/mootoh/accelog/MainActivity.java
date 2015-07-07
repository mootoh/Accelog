package net.mootoh.accelog;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
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


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private boolean isTracking = false;
    SensorManager sensorManager;
    Sensor accelSensor;
    TextView textViewX, textViewY, textViewZ;
    private StringBuffer buffer;
    CanvasView canvasView;
    private Sensor rotationVectorSensor;

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

        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

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

    float[] mRotationMatrix = new float[16];

    SensorEventListener accelListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
                    /*
                    float x = cutLow(sensorEvent.values[0]);
                    float y = cutLow(sensorEvent.values[1]);
                    float z = cutLow(sensorEvent.values[2]);
                    */
            float[] xyza = new float[4];
            float[] src = new float[4];
            src[0] = sensorEvent.values[0];
            src[1] = sensorEvent.values[1];
            src[2] = sensorEvent.values[2];
            src[3] = 0.0f;
            Matrix.multiplyMV(xyza, 0, mRotationMatrix, 0, src, 0);
            float x = cutLow(xyza[0]);
            float y = cutLow(xyza[1]);
            float z = cutLow(xyza[2]);
            textViewX.setText("" + cutLow(sensorEvent.values[0]));
            textViewY.setText("" + cutLow(sensorEvent.values[1]));
            textViewZ.setText("" + cutLow(sensorEvent.values[2]));

            Date now = new Date();
            buffer.append(now.toString() + "," + x + "," + y + "," + z + "\n");
//        Log.d(TAG, now.toString() + "," + x + "," + y + "," + z);

            canvasView.setXYZ(x, y, z);
            canvasView.invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        float cutLow(float value) {
            return (Math.abs(value) < 0.25f) ? 0.0f : value;
        }
    };

    SensorEventListener rotationListener = new SensorEventListener() {
        float cutLow(float value) {
            return (Math.abs(value) < 0.05f) ? 0.0f : value;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values = new float[3];
            values[0] = cutLow(sensorEvent.values[0]);
            values[1] = cutLow(sensorEvent.values[1]);
            values[2] = cutLow(sensorEvent.values[2]);

//            Log.d(TAG, "rot: " + values[0] + ", " + values[1] + ", " + values[2]);

            SensorManager.getRotationMatrixFromVector(mRotationMatrix, values);
//                    SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
            float[] mOrientation = new float[9];
            SensorManager.getOrientation(mRotationMatrix, mOrientation);

            float mPitch = (float) Math.toDegrees(mOrientation[1]);

            float magneticHeading = (float) Math.toDegrees(mOrientation[0]);
//            Log.d(TAG, "pitch, heading = " + mPitch + ", " + magneticHeading);
//                    float mHeading = mod(computeTrueNorth(magneticHeading), 360.0f) - ARM_DISPLACEMENT_DEGREES;
        }

        public int mod(int a, int b) {
            return (a % b + b) % b;
        }

        /*
                        private float computeTrueNorth(float heading) {
                            if (mGeomagneticField != null) {
                                return heading + mGeomagneticField.getDeclination();
                            } else {
                                return heading;
                            }
                        }
        */
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    void toggleMode(final Button btn) {
        if (isTracking) {
            sensorManager.unregisterListener(accelListener);
            sensorManager.unregisterListener(rotationListener);
            btn.setText("Start");
            writeBufferToFile();
        } else {
            sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
//            sensorManager.registerListener(this, accelSensor, (int)1e6);
            sensorManager.registerListener(rotationListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

            buffer = new StringBuffer();
            btn.setText("Stop");
        }

        isTracking = ! isTracking;
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
