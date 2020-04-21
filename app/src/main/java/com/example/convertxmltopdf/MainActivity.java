package com.example.convertxmltopdf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    Button btnImport;
    private int PICKFILE = 1;
    static final int REQUEST_PICKFILE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnImport =  findViewById(R.id.btn_Convert);
        btnImport.setOnClickListener(new View.OnClickListener() {
            //static final int REQUEST_IMAGE_CAPTURE = 2;
            public void onClick(View view) {
                selectFile();
            }
        });
    }
    private void selectFile() {
        Intent intent = new Intent();
        intent.setType("text/xml/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecione um arquivo XML"), PICKFILE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            Log.i("taglegal", filePath.getPath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(filePath, "text/xml/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }
}
