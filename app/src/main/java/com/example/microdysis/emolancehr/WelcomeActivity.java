package com.example.microdysis.emolancehr;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by hui-jou on 6/27/17.
 */

public class WelcomeActivity extends AppCompatActivity {
    /** Called when the activity is first created. */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome); //The content to display from page2.xml file


        Thread WelcomeScreen = new Thread() { //Must create a thread, because opening another page is a different event, as sync
            public void run() {
                try { //This is try-catch
                     sleep(1000);//login
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    //After waiting, shown the content from WelcomePage.java file
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    finish(); //We finish this thread.
                }
            }
        };
        WelcomeScreen.start(); //Starting the thread created.
    }

   }