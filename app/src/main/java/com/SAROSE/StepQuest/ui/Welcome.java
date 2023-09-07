package com.SAROSE.StepQuest.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.fragment.app.FragmentActivity;

import com.SAROSE.StepQuest.R;

public class Welcome extends FragmentActivity {

    Button getStarted;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        getStarted = (Button) findViewById(R.id.getStarted);
        videoView = (VideoView) findViewById(R.id.videoView);

        String path = "android.resource://com.SAROSE.StepQuest/"+R.raw.bg;
        Uri u = Uri.parse(path);
        videoView.setVideoURI(u);
        videoView.start();

        // set loop for video
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

//      set onclicklistner method for get start button
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this, GenderSelection.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        videoView.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        videoView.suspend();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        videoView.stopPlayback();
        super.onDestroy();
    }

}