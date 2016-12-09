package com.example.tief.blockgamezandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OpenView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_view);

        final Button button = (Button) findViewById(R.id.generateNewWalletButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(OpenView.this, MainActivity.class);
                OpenView.this.startActivity(myIntent);
            }
        });




    }
}
