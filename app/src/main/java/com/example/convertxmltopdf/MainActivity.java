package com.example.convertxmltopdf;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.text.LineBreaker;
import android.graphics.text.MeasuredText;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int READ_REQUEST_CODE = 42;
//    private static final int WRITE_REQUEST_CODE = 43;

    Button btnImport;
    Button btnExport;
    TextView lbFilename;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_STORAGE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //request permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }

        btnImport = (Button) findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener() {
            //static final int REQUEST_IMAGE_CAPTURE = 2;
            public void onClick(View view) {
                selectFile();
            }
        });
        lbFilename = (TextView) findViewById(R.id.lbFilename);
        btnExport = (Button) findViewById(R.id.btnExport);
        
        btnExport.setOnClickListener(new View.OnClickListener() {
            //static final int REQUEST_IMAGE_CAPTURE = 2;
            public void onClick(View view) {
                //createFile("application/pdf", "arquivo.pdf");
            }
        });
    }

    private void selectFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/xml/*");
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, "Selecione um arquivo XML"), READ_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Nenhum arquivo encontrado",Toast.LENGTH_LONG).show();
        }
    }

//    private void createFile(String mimeType, String fileName) {
//        try{
//            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//
//            // Filter to only show results that can be "opened", such as
//            // a file (as opposed to a list of contacts or timezones).
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//            // Create a file with the requested MIME type.
//            intent.setType(mimeType);
//
//            intent.putExtra(Intent.EXTRA_TITLE, fileName);
//            startActivityForResult(intent, WRITE_REQUEST_CODE);
//        } catch(Exception e) {
//            Toast.makeText(getApplicationContext(),"Erro ao salvar arquivo",Toast.LENGTH_LONG).show();
//        }
//    }

    //@RequiresApi(api = Build.VERSION_CODES.Q)
    @RequiresApi(api = Build.VERSION_CODES.O)
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
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try{
                Uri uri = data.getData();
                String path = uri.getPath();
                path = path.substring(path.indexOf(":")+1);

                Path p = FileSystems.getDefault().getPath(path);
                if (!p.isAbsolute()) {
                    path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path;
                }

                // mostra o nome do arquivo e exibe botão
                lbFilename.setText(path.split("/")[path.split("/").length-1]);
                //btnExport.setVisibility(View.VISIBLE);

                String textoXML = readTextFile(path);
                createPdf(textoXML);
            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Ocorreu um erro ao selecionar o arquivo!", Toast.LENGTH_SHORT).show();
        }
    }

    private String readTextFile(String filePath) {
        File file = new File(filePath);
        StringBuilder text = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line).append("\n");
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return text.toString();
    }

    private void createPdf(String sometext){
        // create a new document
        PdfDocument document = new PdfDocument();
        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(900, 600, 1).create();
        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//       canvas.drawCircle(50, 50, 30, paint);
//        paint.setColor(Color.BLACK);

        int x = 50, y = 50;
        TextPaint mTextPaint=new TextPaint();
        for (String line: sometext.split("\n")) {
            canvas.drawText(line, x, y, mTextPaint);
            y += mTextPaint.descent() - mTextPaint.ascent();
        }

        //canvas.drawText(sometext, 80, 50, paint);
        // finish the page
        document.finishPage(page);
        // write the document content
        String directory_path = Environment.getExternalStorageDirectory().getPath();
        //String directory_path = Environment.getExternalStorageDirectory().getPath() + "/mypdf/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }else{
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
        String targetPdf = directory_path+"/test-2.pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
            openPDF(filePath);
        } catch (IOException e) {
            Log.e("main", "error "+e.toString());
            Toast.makeText(this, "Something wrong: " + e.toString(),  Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void createPDFTeste(String text){
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        Paint bigPaint = new Paint();
        bigPaint.setTextSize((float) (paint.getTextSize() * 2.0));
        //String text = "Hello, Android.";

        // Prepare the measured text
        MeasuredText mt = new MeasuredText.Builder(text.toCharArray())
                .appendStyleRun(paint, 7, false)  // Use paint for "Hello,
                .appendStyleRun(bigPaint, 8, false)  // Use bigPaint for "Hello, "
                .build();

        LineBreaker lb = new LineBreaker.Builder()
                // Use simple line breaker
                .setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE)
                // Do not add hyphenation.
                .setHyphenationFrequency(LineBreaker.HYPHENATION_FREQUENCY_NONE)
                // Build the LineBreaker
                .build();

        LineBreaker.ParagraphConstraints c = new LineBreaker.ParagraphConstraints();
        c.setWidth(240);  // Set the line wieth as 1024px

        // Do the line breaking
        LineBreaker.Result r = lb.computeLineBreaks(mt, c, 0);

        // Compute the total height of the text.
        int totalHeight = 0;
        for (int i = 0; i < r.getLineCount(); ++i) {  // iterate over the lines
            totalHeight += r.getLineDescent(i) - r.getLineAscent(i);
        }

        // Draw text to the canvas
        Bitmap bmp = Bitmap.createBitmap(240, totalHeight, Bitmap.Config.ARGB_8888);
        //Canvas c2 = new Canvas(bmp);
        float yOffset = 0f;
        int prevOffset = 0;
        for (int i = 0; i < r.getLineCount(); ++i) {  // iterate over the lines
            int nextOffset = r.getLineBreakOffset(i);
            canvas.drawText(text, prevOffset, nextOffset, 0f, yOffset, paint);

            prevOffset = nextOffset;
            yOffset += r.getLineDescent(i) - r.getLineAscent(i);
        }



        document.finishPage(page);
        // write the document content
        String directory_path = Environment.getExternalStorageDirectory().getPath();
        //String directory_path = Environment.getExternalStorageDirectory().getPath() + "/mypdf/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }else{
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
        String targetPdf = directory_path+"/test-2.pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("main", "error "+e.toString());
            Toast.makeText(this, "Something wrong: " + e.toString(),  Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();
    }
    public void openPDF(File filePath){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(filePath), "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
