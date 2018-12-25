package com.project.sih.ambulancebookingapplication;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverDecision extends AppCompatActivity {

    Button acceptButton, rejectButton;
    TextView textView;
    String URL;
    DatabaseReference users_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_decision);

        users_root = FirebaseDatabase.getInstance().getReference("Users");

        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);
        textView = findViewById(R.id.textView);

        Intent intent = getIntent();

        String mappingDetails[] = intent.getStringArrayExtra("Mapping Details");

        String sender_latitude = mappingDetails[0], sender_longitude = mappingDetails[1];
        String receiver_latitude = mappingDetails[2], receiver_longitude = mappingDetails[3];
        String body = mappingDetails[4];
        final String targetRegToken = mappingDetails[5];
        final String senderPhoneNumber = mappingDetails[6], receiverPhoneNumber = mappingDetails[7];

        textView.setText(body);

        Log.d("RegToken", targetRegToken);

        String origin = receiver_latitude+","+receiver_longitude;
        String destination = sender_latitude+","+sender_longitude;

        URL = "https://www.google.com/maps/dir/?api=1&travelmode=driving&dir_action=navigate&destination="+destination+"&origin="+origin;

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageSender.getInstance().sendMessage(targetRegToken, "The driver has accepted your requested, will be arriving soon " +
                        "with the ambulance.", "Request Accepted!," + "Rider," + "null," + "null," + "null," + "null," + "null," +
                senderPhoneNumber + "," + receiverPhoneNumber);


                Uri location = Uri.parse(URL);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
                startActivity(mapIntent);

                makeDriverGoOffline(senderPhoneNumber);

                finish();
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageSender.getInstance().sendMessage(targetRegToken, "Sorry, the driver has denied your request",
                        "Request Denied!," + "Rider," + "null," + "null," + "null," + "null," + "null," + senderPhoneNumber + ","
                + receiverPhoneNumber);

                makeDriverGoOffline(senderPhoneNumber);

                finish();
            }
        });
    }

    private void makeDriverGoOffline(String senderPhoneNumber) {
        users_root.child(senderPhoneNumber).setValue("Offline");
    }
}
