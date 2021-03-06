package com.example.minwoo.pingpongchat_client;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.minwoo.pingpongchat_client.data.model.Data;
import com.example.minwoo.pingpongchat_client.data.model.Notification;
import com.example.minwoo.pingpongchat_client.data.model.PushData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);
    private RetrofitBuilder.PingPongService mPingPongService;
    public UserInfo userInfo;

    public AudioRecord mAudioRecord = null;

    public Thread mRecordThread = null;
    public boolean isRecording = false;

    public AudioTrack mAudioTrack = null;
    public Thread mPlayThread = null;
    public boolean isPlaying = false;

    public String mFilepath = null;
    public String sFilepath = null;

    public ImageView mIV_record = null;

    public String personName;
    public String personEmail;

    public DateFormat dateFormat;
    public Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIV_record = (ImageView) findViewById(R.id.record);

        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecord.startRecording();

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);

        RetrofitBuilder retrofitBuilder = new RetrofitBuilder();
        mPingPongService = retrofitBuilder.getService();

        userInfo = (UserInfo) getIntent().getSerializableExtra(UserInfo.EXTRA);

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();

        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();
        }
        Log.d("FromTo", "from: " + personName + " to: " + userInfo.getPersonName());
    }

    public void onRecord(View view) {
        // should be get from user info later
        final String from = personName;
        final String to = userInfo.getPersonName();

        if (isRecording == true) {
            isRecording = false;
            AnimatedVectorDrawable animatedVectorDrawable =
                    (AnimatedVectorDrawable) getDrawable(R.drawable.anim_vector_stop_to_record);
            mIV_record.setImageDrawable(animatedVectorDrawable);
            animatedVectorDrawable.start();

        } else {
            isRecording = true;
            AnimatedVectorDrawable animatedVectorDrawable =
                    (AnimatedVectorDrawable) getDrawable(R.drawable.anim_vector_record_to_stop);
            mIV_record.setImageDrawable(animatedVectorDrawable);
            animatedVectorDrawable.start();

            mRecordThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] readData = new byte[mBufferSize];
                    mFilepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + from + "To" + to + ".wav";
                    FileOutputStream fos = null;

                    try {
                        fos = new FileOutputStream(mFilepath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    while (isRecording) {
                        int ret = mAudioRecord.read(readData, 0, mBufferSize);
                        Log.d(TAG, "read bytes is" + ret);

                        try {
                            fos.write(readData, 0, mBufferSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;

                    try {
                        fos.close();
                        // test uploading file
                        uploadFile(mFilepath, from, to);
                        JsonObject pushData = createPushData(from);
                        requestPushData(pushData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (mAudioRecord == null) {
                mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
                mAudioRecord.startRecording();
            }
            mRecordThread.start();
        }
    }

    public void onServerPlay(View view) {
        // should be get from user info later
        final String from = userInfo.getPersonName();
        final String to = personName;
        sFilepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + from + "To" + to + ".wav";

        Call<ResponseBody> call = mPingPongService.getRecordFrom(from, to);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                boolean writtenToDisk = HandleResponse.writeResponseBodyToDisk(response.body(), from, to);
                Log.v("getRecordFrom success", "success : " + writtenToDisk);

                if (isPlaying == true) {
                    isPlaying = false;
                } else {
                    isPlaying = true;
                    mPlayThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            byte[] writeData = new byte[mBufferSize];
                            FileInputStream fis = null;
                            try {
                                fis = new FileInputStream(sFilepath);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            DataInputStream dis = new DataInputStream(fis);
                            mAudioTrack.play();

                            while (isPlaying) {
                                try {
                                    int ret = dis.read(writeData, 0, mBufferSize);
                                    if (ret <= 0) {
                                        (MainActivity.this).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                isPlaying = false;
                                            }
                                        });

                                        break;
                                    }
                                    mAudioTrack.write(writeData, 0, ret);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            mAudioTrack.stop();
                            mAudioTrack.release();
                            mAudioTrack = null;

                            try {
                                dis.close();
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    if (mAudioTrack == null) {
                        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                mSampleRate,
                                mChannelCount,
                                mAudioFormat,
                                mBufferSize,
                                AudioTrack.MODE_STREAM
                        );
                    }
                    mPlayThread.start();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("getRecordFrom fail:", t.getMessage());
            }
        });
    }

    // upload file to server
    private void uploadFile(String path, String from, String to) {

        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = new File(path);
        String strFileName = file.getName();
        Uri uri = Uri.fromFile(file);

        Log.d("pingpongTest", strFileName);
        Log.d("pingpongTest2", uri.toString());

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("audio/wav"),
//                        MediaType.parse(getApplicationContext().getContentResolver().getType(uri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("audio", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        // finally, execute the request
        Call<ResponseBody> call = mPingPongService.sendRecord(description, body, from, to);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    private JsonObject createPushData(String from) {
        PushData pushData = new PushData();
        pushData.setNotification(new Notification());
        pushData.setData(new Data());
        pushData.setTo(userInfo.getPersonEmail());
        pushData.setPriority("high");
        pushData.setRestrictedPackageName("com.example.minwoo.pingpongchat_client");
        pushData.getNotification().setTitle(from+"님이 메시지를 보냈습니다.");
        pushData.getNotification().setBody("새로운 메시지가 도착했습니다.");
        pushData.getData().setDate(dateFormat.format(date));
        pushData.getData().setTitle(from+"님이 메시지를 보냈습니다.");
        pushData.getData().setMessage("새로운 메시지가 도착했습니다.");

        Gson gson = new Gson();
        String jsonString = gson.toJson(pushData);
        JsonObject convertedObject = new Gson().fromJson(jsonString, JsonObject.class);

        Log.d("pushdata", "pushData to :" + pushData.getTo());
        Log.d("pushdata", "pushData data :" + pushData.getData().getDate());

        return convertedObject;
    }

    private void requestPushData(JsonObject data) {
        Call<JsonArray> request = mPingPongService.push_data(data);
        request.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                Log.d("PushData Success", response.toString());
                Toast.makeText(MainActivity.this, "PushData 서버 전송 성공", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.d("PushData Fail", t.toString());
                Toast.makeText(MainActivity.this, "PushData 서버 전송 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
