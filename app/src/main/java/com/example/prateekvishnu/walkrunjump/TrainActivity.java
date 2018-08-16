package com.example.prateekvishnu.walkrunjump;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;


public class TrainActivity extends AppCompatActivity {

    private svm_parameter parameter;
    private svm_problem data_values;
    private int cross_validation;
    private int nr_fold;
    private double accuracy_value =0;
    private String result = "";
    String modellocation = Environment.getExternalStorageDirectory() + File.separator + "Android/Data/CHALLENGE1" + File.separator;
    private static double toFloat(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }

    private static int toInt(String s)
    {

        return Integer.parseInt(s);
    }

    public void parameters()
    {

        parameter = new svm_parameter();
        parameter.svm_type = svm_parameter.C_SVC;
        parameter.kernel_type = svm_parameter.POLY;
        parameter.eps = 1e-2;
        parameter.p = 0.1;
        parameter.shrinking = 1;
        parameter.probability = 0;
        parameter.nr_weight = 0;

        parameter.degree = 2;
        parameter.gamma = 0.007;
        parameter.coef0 = 0;
        parameter.nu = 0.5;
        parameter.cache_size = 100;
        parameter.C = 10000;

        parameter.weight_label = new int[0];
        parameter.weight = new double[0];
        cross_validation = 0;
        nr_fold = 3;
    }

    public void set_data() throws IOException
    {
        Reader is = new InputStreamReader(getAssets().open("activity_data_final") );
        BufferedReader br = new BufferedReader(is);
        Vector<Double> vy = new Vector<Double>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        while(true)
        {
            String line = br.readLine();
            if(line == null) break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            vy.addElement(toFloat(st.nextToken()));
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = toInt(st.nextToken());
                x[j].value = toFloat(st.nextToken());
            }
            if(m>0) max_index = Math.max(max_index, x[m-1].index);
            vx.addElement(x);
        }

        data_values = new svm_problem();
        data_values.l = vy.size();
        data_values.x = new svm_node[data_values.l][];


        for(int i = 0; i< data_values.l; i++)
            data_values.x[i] = vx.elementAt(i);
        data_values.y = new double[data_values.l];
        for(int i = 0; i< data_values.l; i++)
            data_values.y[i] = vy.elementAt(i);


        br.close();
    }

    private void cross_validation()
    {
        int i;
        int total_correct = 0;
        double[] target = new double[data_values.l];

        svm.svm_cross_validation(data_values, parameter,nr_fold,target);
        total_correct = 0;

        for(i=0; i< data_values.l; i++)
            if(target[i] == data_values.y[i])
                ++total_correct;
        accuracy_value = 100.0*total_correct/ data_values.l;


        Toast.makeText(getBaseContext(), "Cross Validation Accuracy = "+100.0*total_correct/ data_values.l+"%\n", Toast.LENGTH_LONG).show();

    }

    public void do_predict() throws IOException
    {
        svm_model model = svm.svm_load_model(modellocation+"trainmodel.txt");
        Reader is = new InputStreamReader(new FileInputStream(Environment.getExternalStorageDirectory()+"/Android/Data/CHALLENGE1/predict.txt"));
        BufferedReader br = new BufferedReader(is);
        //int max_index = 0;
        //Vector<svm_node[]> vx = new Vector<svm_node[]>();
        while(true){
            String line = br.readLine();
            if(line == null) break;
            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            int m = st.countTokens()/2;

            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++) {
                x[j] = new svm_node();
                x[j].index = toInt(st.nextToken());
                x[j].value = toFloat((st.nextToken()));
            }
            //  if(m>0) max_index = Math.max(max_index, predictexercise[m-1].index);
            //vx.addElement(predictexercise);


            double predictValue =svm.svm_predict(model,x);
            if(predictValue == 1.0) result = "walking";
            if(predictValue == 2.0) result = "running";
            if(predictValue == 3.0) result = "jumping";
            // br.close();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        TextView t = (TextView) findViewById(R.id.parameters);
        t.setText("SVM Classifier\n" +
                "kernel_type = svm_parameter.POLY; - Polynomial kernel\n" +
                "degree = 2;\n" +
                "gamma = 0.007;\n" +
                "cache_size = 100;\n" +
                "C = 10000;\n" +
                "nr_fold =3;\n\n");

        TextView tv = (TextView)findViewById(R.id.accuracy);

        try {

            parameters();
            set_data();

            String error_msg = svm.svm_check_parameter(data_values, parameter);

            if(error_msg != null)
                Toast.makeText(getBaseContext(), error_msg, Toast.LENGTH_LONG).show();

            if(cross_validation != 0)
                cross_validation();
            else
            {
                svm_model model = svm.svm_train(data_values, parameter);

                svm.svm_save_model(modellocation+"trainmodel.txt",model);
                do_predict();
                //Intent intent = new Intent(TrainActivity.this, PredictActivity.class);
                //Bundle bundle = new Bundle();
                //bundle.putSerializable("key",model);
                //intent.putExtras(bundle);
                //startActivity(intent);

            }

            tv.setText(""+ result);

        }


        catch(Exception ex) {
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
