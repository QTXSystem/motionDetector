package qtx.motiondetector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener, StepListener {
    private static final float THRESHOLD = (float)11.7;
    private static final int SAMPLE_SIZE = 5;
    private SimpleStepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;

    private int numSteps = 0;

    private TextView stepText;

    private TextView deltaText;
    private TextView msgText;
    private Button  start;
    private Button  stop;

    Chronometer myChronometer;
    private boolean startMeasure = false;
    private float mAccelCurrent = 0;
    private float mAccelLast = 0;
    private long lastMotionTime = 0;
    private List<sensorData> sensorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //    TextView myView = (TextView) findViewById(R.layout.activity_pedometer);


        stepText = (TextView) findViewById(R.id.steps);

        msgText = (TextView) findViewById(R.id.msg);
        deltaText = (TextView) findViewById(R.id.delta);
        myChronometer = (Chronometer)findViewById(R.id.chronometer);

        start = (Button)findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numSteps = 0;
                startMeasure = true;
                msgText.setText("Started");
                stepText.setText("0");

                myChronometer.setBase(SystemClock.elapsedRealtime());
                myChronometer.start();
            }
        });

        stop = (Button)findViewById(R.id.stop_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startMeasure = false;
                msgText.setText("Stopped");
                myChronometer.stop();
            }
        });
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new SimpleStepDetector();
        simpleStepDetector.registerListener(this);

        lastMotionTime = -0xffffff;
    }

    @Override
    public void onResume() {
        super.onResume();
        numSteps = 0;

        stepText.setText("0");
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            boolean gotStep = simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);

            if (!startMeasure)
                return;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float)Math.sqrt(x*x + y*y + z*z);

            sensorData data = new sensorData();
            data.x = x;
            data.y = y;
            data.z = z;
            data.timestamp = event.timestamp;
            data.accel = mAccelCurrent;

            if (sensorList.size() ==SAMPLE_SIZE)
                sensorList.remove(0);
            sensorList.add(data);
            boolean motionFound = true;
            String motion = "";
            if (sensorList.size() ==SAMPLE_SIZE){
                for (int i=0; i<SAMPLE_SIZE; i++){
                    if (sensorList.get(i).accel < THRESHOLD) {
                        motionFound = false;
                        break;
                    }
                    // motion += String.format("%.5f,", sensorList.get(i).accel);
                    motion = "raising/sitting";
                    lastMotionTime = System.currentTimeMillis();
                }
                //clear samples
                if (motionFound)
                    sensorList.clear();

            }
            else
                motionFound = false;

            if (motionFound == true) {
                int n = motion.indexOf("raising/sitting");
                if (n < 0)
                    motion += " raising/sitting";
                deltaText.setText(motion);
                return;
            }

            if (gotStep)
                processStep();
        }
    }

    @Override
    public void step(long timeNs) {

    }

    public void processStep() {
        if (!startMeasure)
            return;

        numSteps++;
        String str =String.format("%d", numSteps);
        stepText.setText(str);

        String text =deltaText.getText().toString();
        int n = text.indexOf("walking");
        if (n < 0)
            text += " walking";
        // long curr = System.currentTimeMillis();
        // if ((curr - lastMotionTime) >= 500)
        text = text.replace("raising/sitting", "");
        deltaText.setText(text);


    }
    public class sensorData{
        public float x;
        public float y;
        public float z;
        public long timestamp;
        public float accel;
    }
}


