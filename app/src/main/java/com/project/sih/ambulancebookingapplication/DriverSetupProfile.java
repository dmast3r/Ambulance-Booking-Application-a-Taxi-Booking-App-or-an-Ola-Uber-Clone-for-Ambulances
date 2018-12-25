package com.project.sih.ambulancebookingapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DriverSetupProfile extends AppCompatActivity {

    private Uri filePath1, filePath2;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int REQUEST_CAMERA = 1;
    private final int SELECT_FILE = 2;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView imageView1, imageView2;
    private Button chooseButton1, chooseButton2, uploadButton, continueButton;
    private CheckBox doctorAvailabilityCheck, staffAvailabilityCheck;
    private EditText staffNumberEditText;
    private RadioGroup radioGroup;
    private TextView nameTextView;
    private CheckBox lifeSupportCheckBox, ecgMachineCheckBox, oxygenCylindersCheckBox;

    // data for permission and image upload decisions
    private boolean permissionResult = false;
    private boolean uploadOnImage1;
    private boolean photosUploaded = false;

    // extract some data from shared preference
    SharedPreferences sharedPreferences;
    String phoneNumber;
    String id;
    String associatedWith = "Hospital";
    DatabaseReference driver_root;
    String name, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_setup_profile);

        photosUploaded = savedInstanceState != null && savedInstanceState.getBoolean("photosUploaded");

        Intent intent = getIntent();
        String details[] = intent.getStringArrayExtra("details");
        name = details[0];
        category = details[1];

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        driver_root = MainActivity.databaseReferenceRootDriver;

        // extract some data from shared preference
        sharedPreferences = getSharedPreferences(MainActivity.USER_FILE_KEY, MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("user_number", null);
        id = sharedPreferences.getString("user_id", null);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            getCameraPermission();
        } else permissionResult = true;

        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        chooseButton1 = findViewById(R.id.chooseButton1);
        chooseButton2 = findViewById(R.id.chooseButton2);
        uploadButton = findViewById(R.id.uploadButton);
        doctorAvailabilityCheck = findViewById(R.id.doctorAvailabilityCheck);
        staffAvailabilityCheck = findViewById(R.id.staffAvailabilityCheck);
        staffNumberEditText = findViewById(R.id.staffNumberEditText);
        radioGroup = findViewById(R.id.radioGroup);
        continueButton = findViewById(R.id.continueButton);
        nameTextView = findViewById(R.id.nameTextView);
        lifeSupportCheckBox = findViewById(R.id.lifeSupportCheckBox);
        ecgMachineCheckBox = findViewById(R.id.ecgMachineCheckBox);
        oxygenCylindersCheckBox = findViewById(R.id.oxygenCylindersCheckBox);

        nameTextView.setText(name+"'s details:");

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.hospitalRadioButton)
                    associatedWith = "Hospital";
                else
                    associatedWith = "Government";
            }
        });

        staffNumberEditText.setVisibility(View.GONE);
        staffAvailabilityCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    staffNumberEditText.setVisibility(View.VISIBLE);
                else
                    staffNumberEditText.setVisibility(View.GONE);
            }
        });


        chooseButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadOnImage1 = true;
                chooseImage();
            }
        });

        chooseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadOnImage1 = false;
                chooseImage();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((imageView1.getDrawable() == null && imageView2.getDrawable() == null) || (staffAvailabilityCheck.isChecked() &&
                staffNumberEditText.getText().toString().length() == 0) || !photosUploaded)
                    Toast.makeText(DriverSetupProfile.this, "Can't continue before you submit all the data properly, make sure you" +
                                    "are doing it. Also ensure that photos have been uploaded to our server(you get a confirmation for this).",
                            Toast.LENGTH_LONG).show();

                else
                    submitData();
            }
        });
    }

    private void chooseImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverSetupProfile.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    if(permissionResult)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    if(permissionResult)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    // upload both the images to firebase cloud, this will first check if the user has selected/captured images
    private void uploadImage() {
        if(imageView1.getDrawable() == null || imageView2.getDrawable() == null)
            Toast.makeText(DriverSetupProfile.this, "Choose images for bothe Certificate and License first!",
                    Toast.LENGTH_LONG).show();

        else {
            uploadImageUtil(filePath1, "Certificate");
            uploadImageUtil(filePath2, "Licence");
        }
    }

    // A utility function for uploading the images to cloud
    private void uploadImageUtil(Uri filePath, final String name) {
        if(filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("Certificates and Licenses/"+ phoneNumber + "/" + name);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(DriverSetupProfile.this, "Uploaded " + name + " Image", Toast.LENGTH_SHORT).show();
                            photosUploaded = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(DriverSetupProfile.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            photosUploaded = false;
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    // use the Dexter library to obtain permission for Camera and Storage read-write
    private void getCameraPermission() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            permissionResult = true;
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            permissionResult = false;
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    // call to take pictures from camera
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    // call to select images from the gallary
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    // set on the image view, the image selected from the gallery
    private void onSelectFromGalleryResult(Intent data) {
        ImageView imageView = uploadOnImage1 ? imageView1 : imageView2;

        Bitmap bm = null;
        if (data != null) {
            try {
                if(uploadOnImage1)
                    filePath1 = data.getData();
                else
                    filePath2 = data.getData();

                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imageView.setImageBitmap(bm);
    }

    // set on the image view, the image captured from the camera
    private void onCaptureImageResult(Intent data) {
        ImageView imageView = uploadOnImage1 ? imageView1 : imageView2;

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".png");

        if(uploadOnImage1)
            filePath1 = Uri.fromFile(destination);
        else
            filePath2 = Uri.fromFile(destination);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageView.setImageBitmap(thumbnail);
    }

    // submit data and continue
    private void submitData() {
        DatabaseReference databaseReference = driver_root.child(phoneNumber);
        databaseReference.child("Associated With").setValue(associatedWith);
        databaseReference.child("Doctor Availability").setValue(doctorAvailabilityCheck.isChecked());
        databaseReference.child("Staff Availability").setValue(staffAvailabilityCheck.isChecked());
        databaseReference.child("Staff Number").setValue(staffAvailabilityCheck.isChecked() ? staffNumberEditText.getText().toString() :
        0);
        databaseReference.child("Name").setValue(name);

        StringBuilder facilitiesBuffer = new StringBuilder();
        if(lifeSupportCheckBox.isChecked())
            facilitiesBuffer.append("Life Support System ");
        if(ecgMachineCheckBox.isChecked())
            facilitiesBuffer.append("ECG Machine ");
        if(oxygenCylindersCheckBox.isChecked())
            facilitiesBuffer.append("Oxygen Cylinders ");

        String facilities = facilitiesBuffer.toString();
        databaseReference.child("Facilities").setValue(facilities);

        DriverHome.shouldShowDialog = true;
        DriverHome.store_data = true;
        Intent intent = new Intent(DriverSetupProfile.this, DriverHome.class);
        intent.putExtra("details", new String[]{name, category});
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("photosUploaded", photosUploaded);
    }
}
