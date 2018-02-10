package com.example.ytseitkin.audiohelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.inputmethodservice.ExtractEditText;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends Activity implements OnInitListener {

    private TessBaseAPI mTess; //Tess API reference
    String datapath = ""; //path to folder containing language data file

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final int MY_DATA_CHECK_CODE = 1234;
    private Activity activity;
    private TextToSpeech myTTS;
    private Intent checkIntent;
   private String text;
   private EditText et;

    ImageView imageView;

    Bitmap image; //our image

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        imageView = (ImageView) findViewById(R.id.imageView);

        datapath = getFilesDir()+ "/tesseract/";

        //make sure training data has been copied
        checkFile(new File(datapath + "tessdata/"));

        //init Tesseract API
        String language = "eng";

        mTess = new TessBaseAPI();
        mTess.setDebug(true);
        mTess.init(datapath, language);

        Button clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }

//                onClickOfButton(v);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            image = imageBitmap;

            imageView.setImageBitmap(imageBitmap);

            String OCRresult;
            mTess.setImage(imageBitmap);
            OCRresult = mTess.getUTF8Text();

            showToast(OCRresult);
            processTextToSpeech(OCRresult);
        }

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                    myTTS = new TextToSpeech(MainActivity.this, this);
                } else {
                    // missing data, install it
                    Intent installIntent = new Intent();
                    installIntent.setAction(
                            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
        }

    }
    public void processTextToSpeech(String text){
        Intent checkIntent = new Intent();
        TextToSpeech tts= new TextToSpeech(this, (OnInitListener) this);

        //EditText editText = (EditText) findViewById(R.id.editText2);
        //String toSpeak = editText.toString();

        String toSpeak = text;
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }


    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub
        if(status == TextToSpeech.SUCCESS){
            int result=myTTS.setLanguage(Locale.US);
            if(result==TextToSpeech.LANG_MISSING_DATA ||
                    result==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("error", "This Language is not supported");
            }
            else{
                ConvertTextToSpeech();
            }

        } else
            Log.e("error", "Initilization Failed!");
    }

    protected void showToast(String content) {
        Context context = getApplicationContext();
        CharSequence text = content;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    private void checkFile(File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }


    private void copyFiles() {

        //location we want the file to be at
        String filepath = datapath + "/tessdata/eng.traineddata";

        try(OutputStream outstream = new FileOutputStream(filepath)) {

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/eng.traineddata");


            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onPause() {
            // TODO Auto-generated method stub

            if(myTTS != null){

            myTTS.stop();
            myTTS.shutdown();
            }
            super.onPause();
    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        et=(EditText)findViewById(R.id.editText2);
        if (et != null) {
            text = et.getText().toString();
            HashMap Hash = new HashMap();
            Hash.put("Key", text);
            if (text == null || "".equals(text)) {
                text = "";
                myTTS.speak(text, TextToSpeech.QUEUE_FLUSH, Hash);
            } else
                myTTS.speak(text , TextToSpeech.QUEUE_FLUSH, Hash);
        }

    }

}

