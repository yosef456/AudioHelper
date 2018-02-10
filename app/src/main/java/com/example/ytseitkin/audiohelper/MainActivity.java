package com.example.ytseitkin.audiohelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.inputmethodservice.ExtractEditText;
import android.provider.MediaStore;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends Activity implements OnInitListener {


    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final int MY_DATA_CHECK_CODE = 1234;
    private Activity activity;
    private TextToSpeech myTTS;
    private Intent checkIntent;
   private String text;
   private EditText et;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        imageView = (ImageView) findViewById(R.id.imageView);

        Button clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }


                onClickOfButton(v);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

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
    public void onClickOfButton(View view){
        Intent checkIntent = new Intent();
        TextToSpeech tts= new TextToSpeech(this, (OnInitListener) this);
        EditText editText = (EditText) findViewById(R.id.editText2);
        String toSpeak = editText.toString();
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
        }
        else
            Log.e("error", "Initilization Failed!");
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

public void onClick(View v){

        ConvertTextToSpeech();

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

