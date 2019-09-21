package com.sebastianlundquist.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> breedURLs = new ArrayList<>();
    ArrayList<String> breedNames = new ArrayList<>();
    int chosenBreed = 0;
    String[] answers = new String[4];
    Button answer0Button;
    Button answer1Button;
    Button answer2Button;
    Button answer3Button;
    int locationOfCorrectAnswer = 0;
    ImageView imageView;

    public static class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    public void breedChosen(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Wrong! It was " + breedNames.get(chosenBreed) + "!", Toast.LENGTH_SHORT).show();
        }
        newQuestion();
    }

    public void newQuestion() {
        try {
            Random random = new Random();
            chosenBreed = random.nextInt(breedURLs.size());
            ImageDownloader imageDownloader = new ImageDownloader();
            Bitmap breedImage = imageDownloader.execute(breedURLs.get(chosenBreed)).get();
            imageView.setImageBitmap(breedImage);
            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation = 0;
            ArrayList<Integer> alternatives = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = breedNames.get(chosenBreed);
                }
                else {
                    do {
                        incorrectAnswerLocation = random.nextInt(breedURLs.size());
                    }
                    while (incorrectAnswerLocation == chosenBreed || alternatives.contains(incorrectAnswerLocation));
                    answers[i] = breedNames.get(incorrectAnswerLocation);
                }
                alternatives.add(incorrectAnswerLocation);
            }

            answer0Button.setText(answers[0]);
            answer1Button.setText(answers[1]);
            answer2Button.setText(answers[2]);
            answer3Button.setText(answers[3]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result;
        imageView = findViewById(R.id.imageView);

        answer0Button = findViewById(R.id.answer0Button);
        answer1Button = findViewById(R.id.answer1Button);
        answer2Button = findViewById(R.id.answer2Button);
        answer3Button = findViewById(R.id.answer3Button);

        try {
            result = task.execute("https://sebastianlundquist.github.io/dog-breeds/").get();
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(result);

            while (m.find()) {
                breedURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(result);

            while (m.find()) {
                breedNames.add(m.group(1));
            }
            newQuestion();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
