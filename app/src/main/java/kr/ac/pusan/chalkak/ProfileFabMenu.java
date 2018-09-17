package kr.ac.pusan.chalkak;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Emotion;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.Glasses;
import com.microsoft.projectoxford.face.contract.Hair;
import com.microsoft.projectoxford.face.contract.Makeup;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFabMenu extends AppCompatActivity {
    private ProgressDialog detectionProgressDialog;

    private final String apiEndpoint = "https://japaneast.api.cognitive.microsoft.com/face/v1.0";
    private final String subscriptionKey = "44a1c74666714a0e90bafd186472b487";

    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    private TextView textAge, textSmile, textGender, textEmotion, textMakeup, textHair, textGlass, textPersent;
    private ImageView imageSame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_fab_menu);

        Intent intent = getIntent();
        String uri = intent.getStringExtra("path");

        textPersent = findViewById(R.id.textPersent);
        imageSame = findViewById(R.id.imageSame);

        detectionProgressDialog = new ProgressDialog(this);
        detectAndFrame(BitmapFactory.decodeFile(uri));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    // Detect faces by uploading a face image.
    // Frame faces after detection.
    private void detectAndFrame(final Bitmap imageBitmap) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("결과 조회 중...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    // returnFaceAttributes:
                                    new FaceServiceClient.FaceAttributeType[] {
                                            FaceServiceClient.FaceAttributeType.Age,
                                            FaceServiceClient.FaceAttributeType.Smile,
                                            FaceServiceClient.FaceAttributeType.Gender,
                                            FaceServiceClient.FaceAttributeType.Emotion,
                                            FaceServiceClient.FaceAttributeType.Makeup,
                                            FaceServiceClient.FaceAttributeType.Hair,
                                            FaceServiceClient.FaceAttributeType.Glasses
                                    }
                            );

                            if (result == null){
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(String.format(
                                    "Detection Finished. %d face(s) detected",
                                    result.length));
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }

                        if (result == null)
                            return;

                        imageSame.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageBitmap.recycle();

                        List<Face> faces = new ArrayList<>();
                        faces = Arrays.asList(result);

                        if (faces.size() == 0) {
                            Toast.makeText(getApplicationContext(), "얼굴을 감지하지 못했습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        String age = Double.toString(faces.get(0).faceAttributes.age);
                        String smile = Double.toString(faces.get(0).faceAttributes.smile);
                        String gender = faces.get(0).faceAttributes.gender;
                        String emotion = getEmotion(faces.get(0).faceAttributes.emotion);
                        String makeup = getMakeup(faces.get(0).faceAttributes.makeup);
                        String hair = getHair(faces.get(0).faceAttributes.hair);
                        Glasses glass = faces.get(0).faceAttributes.glasses;
                        String sGlass = glass.name();

                        textAge = findViewById(R.id.textAge);
                        textSmile = findViewById(R.id.textSmile);
                        textGender = findViewById(R.id.textGender);
                        textEmotion = findViewById(R.id.textEmotion);
                        textMakeup = findViewById(R.id.textMakeup);
                        textHair = findViewById(R.id.textHair);
                        textGlass = findViewById(R.id.textGlass);

                        textAge.setText(age);
                        textSmile.setText(smile);
                        textGender.setText(gender);
                        textEmotion.setText(emotion);
                        textMakeup.setText(makeup);
                        textHair.setText(hair);
                        textGlass.setText(sGlass);
                    }

                    private String getHair(Hair hair) {
                        if (hair.hairColor.length == 0)
                        {
                            if (hair.invisible)
                                return "Invisible";
                            else
                                return "Bald";
                        }
                        else
                        {
                            int maxConfidenceIndex = 0;
                            double maxConfidence = 0.0;

                            for (int i = 0; i < hair.hairColor.length; ++i)
                            {
                                if (hair.hairColor[i].confidence > maxConfidence)
                                {
                                    maxConfidence = hair.hairColor[i].confidence;
                                    maxConfidenceIndex = i;
                                }
                            }

                            return hair.hairColor[maxConfidenceIndex].color.toString();
                        }
                    }

                    private String getMakeup(Makeup makeup) {
                        return  (makeup.eyeMakeup || makeup.lipMakeup) ? "Yes" : "No" ;
                    }

                    private String getEmotion(Emotion emotion)
                    {
                        String emotionType = "";
                        double emotionValue = 0.0;
                        if (emotion.anger > emotionValue)
                        {
                            emotionValue = emotion.anger;
                            emotionType = "Anger";
                        }
                        if (emotion.contempt > emotionValue)
                        {
                            emotionValue = emotion.contempt;
                            emotionType = "Contempt";
                        }
                        if (emotion.disgust > emotionValue)
                        {
                            emotionValue = emotion.disgust;
                            emotionType = "Disgust";
                        }
                        if (emotion.fear > emotionValue)
                        {
                            emotionValue = emotion.fear;
                            emotionType = "Fear";
                        }
                        if (emotion.happiness > emotionValue)
                        {
                            emotionValue = emotion.happiness;
                            emotionType = "Happiness";
                        }
                        if (emotion.neutral > emotionValue)
                        {
                            emotionValue = emotion.neutral;
                            emotionType = "Neutral";
                        }
                        if (emotion.sadness > emotionValue)
                        {
                            emotionValue = emotion.sadness;
                            emotionType = "Sadness";
                        }
                        if (emotion.surprise > emotionValue)
                        {
                            emotionValue = emotion.surprise;
                            emotionType = "Surprise";
                        }
                        return String.format("%s: %f", emotionType, emotionValue);
                    }
                };

        detectTask.execute(inputStream);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Example data
                final String URL = "http://222.97.247.238:9080/api/test";

                OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build();

                RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), outputStream.toByteArray());

                Request request = new Request.Builder()
                        .url(URL)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String val = jsonObject.get("pose").toString();

                    Log.i("CHALKAK", response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }})
                .create().show();
    }

    private static Bitmap drawFaceRectanglesOnBitmap(
            Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }
}
