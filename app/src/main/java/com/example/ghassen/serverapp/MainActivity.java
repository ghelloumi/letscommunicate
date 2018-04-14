package com.example.ghassen.serverapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private Button btnSpeak;


    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private TextView txtText;

    // URL to get contacts JSON
    private static String url = "http://192.168.1.61:3000/gestures";

    ArrayList<String> wordList;
   // Map<String, String> wordMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        wordList = new ArrayList<>();

        txtText = (TextView) findViewById(R.id.wordTextView);

        new GetWords().execute();


        tts = new TextToSpeech(this, this);
        btnSpeak = (Button) findViewById(R.id.btnSpeak);

        // button on click event
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                speakOut();
            }

        });
    }


    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }


    }
    private void speakOut() {
        String text = txtText.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }



    /**
     * Async task class to get json by making HTTP call
     */
    private class GetWords extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);


            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    String str = jsonObj.getString("name");
                   Log.v("name","name value "+str);
                    wordList.add(0,str);


//
//                    // looping through All Contacts
//                    for (int i = 0; i < words.length(); i++) {
//                        JSONObject w = words.getJSONObject(i);
//
//                        String name = w.getString("name");
//
//
//                        // tmp hash map for single contact
//                        //HashMap<String, String> wordOutput = new HashMap<>();
//
//                        // adding each child node to HashMap key => value
//                       // wordOutput.put("name", name);
//
//                        // adding contact to contact list
//                        wordList.add(name);
//                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            StringBuilder finalSentence=new StringBuilder("");
            for(int i=0; i<wordList.size(); i++) {
                finalSentence.append(wordList.get(i));
            }
            txtText.setText(finalSentence.toString());
        }
    }
}