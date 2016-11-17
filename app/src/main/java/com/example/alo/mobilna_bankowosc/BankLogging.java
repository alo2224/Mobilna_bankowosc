package com.example.alo.mobilna_bankowosc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.sql.*;
import java.util.Properties;

import com.example.alo.mobilna_bankowosc.R;

/**
 * A selection screen that allows user to choose the banks that he wished to log into.
 */
public class BankLogging extends AppCompatActivity {

    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_logging);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            this.email = extras.getString("email");
        }
        Button finishButton = (Button) findViewById(R.id.logFinished);
        ImageButton mBank = (ImageButton) findViewById(R.id.imageButton4);
        finishButton.setOnClickListener( new View.OnClickListener(){
                public void onClick(View v){
                    Intent intent = new Intent(BankLogging.this, MainActivity.class);
                    intent.putExtra("user", email);
                    startActivity(intent);
        }
        });
    }
    /**
     * Starting new activity when user clicks on the logo
     * While also sending the bankId and user email as extra parameters
     */
    public void logToBank(View v) {
        String selectedBank = v.getTag().toString();
        Intent intent = new Intent(this, BankAuth.class);
        intent.putExtra("bankId" , selectedBank);
        intent.putExtra("user",email);
        startActivity(intent);
    }
}
