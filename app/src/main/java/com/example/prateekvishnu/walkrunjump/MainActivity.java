package com.example.prateekvishnu.walkrunjump;



import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;

import com.example.prateekvishnu.walkrunjump.util.FileUtil;
import com.example.prateekvishnu.walkrunjump.util.Activityrecord;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    static Button btn_record;
    static Button btn_convert;
    static Button btn_train;
    static Button btn_start;
    static Button btn_upload;
    static EditText toast_text;
    static String database_name = "Activity Recognition";
    static String table_name = "activity_data";

    String database_location = Environment.getExternalStorageDirectory() + File.separator + "Android/Data/CHALLENGE1" + File.separator + database_name;
    private String basePath = Environment.getExternalStorageDirectory() + "/Android/Data/CHALLENGE1";
    static SQLiteDatabase db;
    SensorManager sensorManager;
    Sensor acclnSensor;
    int accln_count1 = 1;
    int accln_count2 = 1;
    int accln_count3 = 1;

    long update = 0;
    float[] accln_x = new float[50];
    float[] accln_y = new float[50];
    float[] accln_z = new float[50];

    float[] accln_a = new float[50];
    float[] accln_b = new float[50];
    float[] accln_c = new float[50];

    boolean flag_3 = false;
    boolean flag_2 = false;
    boolean flag = false;
    static int id;
    long row = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bmob.initialize(this, "38cd909fe640756176303b980453db55");

        btn_record = (Button) findViewById(R.id.button1);
        btn_convert = (Button) findViewById(R.id.button2);
        btn_train = (Button) findViewById(R.id.button3);
        btn_start = (Button) findViewById(R.id.button4);
        btn_upload = (Button) findViewById(R.id.button5);
        toast_text = (EditText) findViewById(R.id.editText1);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acclnSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) this, acclnSensor, SensorManager.SENSOR_DELAY_NORMAL);

        btn_record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                create_table();
                flag_2 = true;
            }
        });

        btn_convert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    convert_data();
                    Toast.makeText(MainActivity.this, "Database Converted", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_train.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TrainActivity.class);
                startActivity(intent);
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                flag_3 = true;
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                uploadFile();
            }
        });

    }

    public void location(View v) {
        Intent intent = new Intent(MainActivity.this, LocationActivity.class);
        startActivity(intent);
    }


    public void create_table() {
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/Android/Data/CHALLENGE1");
            if (!folder.exists()) {
                folder.mkdir();
            }
            db = SQLiteDatabase.openOrCreateDatabase(database_location, null);
            db.beginTransaction();
            try {
                String sqlCreateTable = "create table " + table_name + "(id integer primary key autoincrement,X";
                for (int i = 1; i <= 50; i++) {
                    if (i == 50) {
                        sqlCreateTable += Integer.toString(i) + " float,Y";
                        sqlCreateTable += Integer.toString(i) + " float,Z";
                        sqlCreateTable += Integer.toString(i) + " float,label varchar(20));";
                    } else {
                        sqlCreateTable += Integer.toString(i) + " float,Y";
                        sqlCreateTable += Integer.toString(i) + " float,Z";
                        sqlCreateTable += Integer.toString(i) + " float,X";
                    }

                }
                db.execSQL(sqlCreateTable);
                db.setTransactionSuccessful();

            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        } catch (SQLException e) {

            Toast.makeText(this, "Error Creating DB - Check Permissions", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        long currtime = System.currentTimeMillis();
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER && flag_2) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            if ((currtime - update > 100) && accln_count3 <= 51) {
                update = currtime;
                accln_x[accln_count3 - 1] = x;
                accln_y[accln_count3 - 1] = y;
                accln_z[accln_count3 - 1] = z;
                accln_count3++;

            }

            if (flag) {
                accln_count1 = 1;
                accln_count2 = 1;
                accln_count3 = 1;
                flag = false;
            }

            if (accln_count3 >= 51) {
                for (int i = 1; i <= 50; i++) {
                    set_table();
                    flag = true;
                }
                Toast.makeText(MainActivity.this, "Table Created", Toast.LENGTH_LONG).show();
                flag_2 = false;
            }
        }

        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER && flag_3) {
            float a = sensorEvent.values[0];
            float b = sensorEvent.values[1];
            float c = sensorEvent.values[2];

            if ((currtime - update > 100) && accln_count3 <= 51) {
                update = currtime;
                accln_a[accln_count3 - 1] = a;
                accln_b[accln_count3 - 1] = b;
                accln_c[accln_count3 - 1] = c;
                accln_count3++;
            }

            if (flag) {
                accln_count1 = 1;
                accln_count2 = 1;
                accln_count3 = 1;
                flag = false;
            }

            if (accln_count3 >= 51) {
                for (int i = 1; i <= 50; i++) {
                    save_predict();
                    flag = true;
                }
                Toast.makeText(MainActivity.this, "Predict finished", Toast.LENGTH_LONG).show();
                flag_3 = false;
            }

        }
        }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, acclnSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void set_table() {
        String input = toast_text.getText().toString();
        db = SQLiteDatabase.openOrCreateDatabase(database_location, null);
        db.beginTransaction();

        try {
            if (accln_count2 == 1) {
                ContentValues values = new ContentValues();
                values.put("label", input);
                values.put("x1", accln_x[accln_count1 - 1]);
                values.put("y1", accln_y[accln_count1 - 1]);
                values.put("z1", accln_z[accln_count1 - 1]);
                row = db.insert(table_name, null, values);
                accln_count2++;
            } else {
                String Update = "UPDATE " + table_name
                        + " SET "
                        + "x" + accln_count1 + " = " + accln_x[accln_count1 - 1] + ", "
                        + "y" + accln_count1 + " = " + accln_y[accln_count1 - 1] + ", "
                        + "z" + accln_count1 + " = " + accln_z[accln_count1 - 1]
                        + " WHERE ID = " + row;
                db.execSQL(Update);
            }
            accln_count1++;
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }


    public void convert_data() throws IOException {
        db = SQLiteDatabase.openOrCreateDatabase(database_location, null);
        File filewrite = new File(Environment.getExternalStorageDirectory() + "/Android/Data/CHALLENGE1", "database.txt");
        Cursor cursor = null;
        String sqlQuery = "select MAX(id) from " + table_name;
        cursor = db.rawQuery(sqlQuery, null);
        cursor.moveToFirst();
        id = cursor.getInt(0);
        FileWriter writer = new FileWriter(filewrite);
        for (int i = 0; i < id; i++) {
            String output = "";
            String sqlquery = "Select * from " + table_name + " where id=" + (i + 1);
            cursor = db.rawQuery(sqlquery, null);
            cursor.moveToFirst();
            String labels = cursor.getString(151);

            if (labels.equalsIgnoreCase("walking"))
                output = "+1 ";
            else if (labels.equalsIgnoreCase("running"))
                output = "+2 ";
            else if (labels.equalsIgnoreCase("jumping"))
                output = "+3 ";


            for (int j = 1; j <= 150; j++)
                output += j + ":" + cursor.getFloat(j) + " ";

            output.trim();
            output += "\n";

            writer.append(output);
            writer.flush();

        }
        writer.close();
    }

        public void save_predict(){
            String content = "";
            //File filewrite = new File(Environment.getExternalStorageDirectory() + "/Android/Data/CHALLENGE1", "predict.txt");
            if(flag_3) {
                for (int j = 1,k = 1,t = 1,s = 1; j <= 50; j++) {
                    k = 1+3*(j-1);
                    t = k+1;
                    s = t+1;
                    content += k + ":" + accln_a[j-1] + " " + t + ":" + accln_b[j-1] + " " + s + ":" + accln_c[j-1] + " ";
                }
                FileUtil.write(basePath, "predict.txt", content, false);
            }
            else {
                Toast.makeText(MainActivity.this, "Haven't recorded data", Toast.LENGTH_LONG).show();
            }
        }

        public void uploadFile(){
            final String filename = "/"+android.os.Build.MODEL+"activity_record.txt";
            final String recordpath = basePath + filename;
            final BmobFile file = new BmobFile(new File(recordpath));
            file.upload(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if(e==null){
                        Activityrecord activityrecord = new Activityrecord();
                        activityrecord.setName(android.os.Build.MODEL);
                        activityrecord.setRecord(file);
                       activityrecord.update(new UpdateListener() {
                           @Override
                           public void done(BmobException e) {
                               if(e==null) Toast.makeText(MainActivity.this, "yeah", Toast.LENGTH_SHORT).show();
                           }
                       });
                        Toast.makeText(MainActivity.this, "Upload Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Upload Filed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

}