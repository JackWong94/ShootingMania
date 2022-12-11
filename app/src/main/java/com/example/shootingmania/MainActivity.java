package com.example.shootingmania;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enterFullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GameView gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    public void onResume(){
        super.onResume();
        enterFullScreen();
    }

    @Override
    public void onBackPressed() {
        //Override to prevent back button from closing the second activity dialog
        enterFullScreen();
    }

    //Start Game Function
    public void startGame(View view) {
        GameView gameView = new GameView(this);
        setContentView(gameView);
    }

    //Show Leaderboard
    public void showLeaderboard(View view) {
    }

    //Exit Game
    public void exitGame(View view) {
        Toast.makeText(this,"Exiting",Toast.LENGTH_SHORT).show();
        finish();
    }

    //Hide navigation bar and make full screen
    public void enterFullScreen() {
        //Make full screen and hide navigation bar
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}