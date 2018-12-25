package com.project.sih.ambulancebookingapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class SetUpProfileActivity extends AppCompatActivity {

    TextView nameText;
    RadioGroup radioGroup;
    Button continueButton, logoutButton;
    String category = "Rider", name;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_profile);

        final FirebaseAuth firebaseAuth = MainActivity.firebaseAuth;

        setContentView(R.layout.activity_set_up_profile);
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.FILE_NAME_KEY), MODE_PRIVATE);

        nameText = findViewById(R.id.nameText);
        radioGroup = findViewById(R.id.radioGroup);
        continueButton = findViewById(R.id.continueButton);
        logoutButton = findViewById(R.id.logoutButton);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.userRadioButton)
                    category = "Rider";
                else
                    category = "Driver";
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nameText.getText().toString();
                if (name.length() < 3)
                    Toast.makeText(SetUpProfileActivity.this, "Name must be atleast 3 characters long", Toast.LENGTH_LONG)
                            .show();
                else {
                    Intent intent;

                    if(category.equals("Rider"))  {
                        Home.store_data = true;
                        intent = new Intent(SetUpProfileActivity.this, Home.class);
                        intent.putExtra("details", new String[]{name, category});
                    }
                    else {
                        DriverHome.shouldShowDialog = true;
                        DriverHome.store_data = true;
                        intent = new Intent(SetUpProfileActivity.this, DriverSetupProfile.class);
                        intent.putExtra("details", new String[]{name, category});
                    }

                    startActivity(intent);
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(SetUpProfileActivity.this, MainActivity.class));
                }
                else
                    Toast.makeText(SetUpProfileActivity.this, "An unknown error occurred!", Toast.LENGTH_LONG).show();
            }
        });
    }

}
