package com.ajayvamsee.noisesuppressor;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

public class MainActivity extends AppCompatActivity {

    private NoiseSuppressor noiseSuppressor;

    private TextView startTV, stopTV, playTV, stopplayTV, statusTV;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    private Button btnRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize all variables with their layout items.
        statusTV = findViewById(R.id.idTVstatus);
        startTV = findViewById(R.id.btnRecord);
        stopTV = findViewById(R.id.btnStop);
        playTV = findViewById(R.id.btnPlay);
        stopplayTV = findViewById(R.id.btnStopPlay);

        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        playTV.setBackgroundColor(getResources().getColor(R.color.gray));
        stopplayTV.setBackgroundColor(getResources().getColor(R.color.gray));


        startTV.setOnClickListener(v -> startRecording());

        stopTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRecording();
            }
        });

        playTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        stopplayTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlaying();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private void startRecording() {
        // check permission method is used to check that the user has granted permission to record nd store the audio.
        if (CheckPermissions()) {

            stopTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
            startTV.setBackgroundColor(getResources().getColor(R.color.gray));
            playTV.setBackgroundColor(getResources().getColor(R.color.gray));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.gray));

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(getFilePath());
            try {
                //below method will prepare our audio recorder class
                mRecorder.prepare();

            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            // start method will start the audio recording.
            mRecorder.start();
            statusTV.setText("Recording Started");
        } else {
            //if audio recording permissions are not granted by user below method will ask for runtime permission for mic and storage.
            RequestPermissions();
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private void pauseRecording() {
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        playTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200));

        // the audio recording.
        mRecorder.stop();

        // the media recorder class.
        mRecorder.release();
        mRecorder = null;
        statusTV.setText("Recording Stopped");
    }

    private void playAudio() {
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        playTV.setBackgroundColor(getResources().getColor(R.color.gray));
        stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200));

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(getFilePath());
            mPlayer.prepare();
            mPlayer.start();
            statusTV.setText("Recording Started Playing");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void pausePlaying() {
        stopTV.setBackgroundColor(getResources().getColor(R.color.gray));
        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        playTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200));

        // below method will stop
        // the audio recording.
        mRecorder.stop();

        // below method will release
        // the media recorder class.
        mRecorder.release();
        mRecorder = null;
        statusTV.setText("Recording Stopped");
    }

    private String getFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, new Timestamp(System.currentTimeMillis()) + " main" + ".pcm");
        Log.d("TAG", "" + file.getPath());
        return file.getPath();

    }


}