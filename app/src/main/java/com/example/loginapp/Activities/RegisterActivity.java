package com.example.loginapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.loginapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    ImageView ImgUserPhoto;
    static  int PReqCode=1;
    static  int REQUESTCODE=1;
    Uri pickedImgUri;


    private EditText userEmail, userPassword,userPassword2,userName;
    private ProgressBar loadingProgress;
    private Button regBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2 = findViewById(R.id.regPassword2);
        userName = findViewById(R.id.regName);
        loadingProgress = findViewById(R.id.progressBar);
        regBtn = findViewById(R.id.regBtn);
        loadingProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = userPassword2.getText().toString();
                final String name = userName.getText().toString();


                if( name.isEmpty()  ){

                    // display error message when something goes wrong
                    showMessage("Name is empty!");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);

                }if(email.isEmpty()){

                    showMessage("Email is empty!");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);

                }if(password.isEmpty()|| password2.isEmpty()){

                    showMessage("Password is empty!");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);

                }if(!password.equals(password2)){

                    showMessage("Passwords do not match!");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);

                }
                if(pickedImgUri == null ){

                    showMessage("No profile picture is selected!");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
                else{
                    //everything is ok and all fields are filled

                    CreateUserAccount(email,name,password);
                }
            }
        });

        ImgUserPhoto = findViewById(R.id.regUserPhoto);

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT>=22){
                    checkAndRequestForPermission();

                }
                else{
                    openGallery();
                }

            }
        });
    }

    private void CreateUserAccount(String email, final String name, String password) {

        //Creates user account
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //user account created successfully
                            showMessage("Account created");
                            //update profile picture after creation
                            updateUserInfo(name,pickedImgUri,mAuth.getCurrentUser());
                        }
                        else{
                            //account creation failed
                            showMessage("Account creation failed" + task.getException().getMessage());
                            regBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });


    }

    //update user photo and name
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {

        //first, upload user photo to firebase database and get uri
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");

        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());

        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //image uploaded successfully
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //uri contain user image uri

                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();


                        currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            //user info updated successfully
                                            showMessage("Register Complete");
                                            updateUI();
                                        }

                                    }
                                });

                    }
                });
            }
        });

    }

    private void updateUI() {

        Intent PostLogin = new Intent(getApplicationContext(), com.example.loginapp.Activities.Home.class);
        startActivity(PostLogin);
        finish();


    }

    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }

    private void openGallery() {
        //Open gallery and wait for user to pick an image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESTCODE);
    }

    private void checkAndRequestForPermission() {

        if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
            !=PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(this, "Please grant the required permission ", Toast.LENGTH_SHORT).show();
            }
            else{
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PReqCode);
            }

        }
        else{
            openGallery();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data !=null){

            //the user has successfully picked an image
            // we need to save its reference to a variable
            pickedImgUri =data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);

        }
    }
}
