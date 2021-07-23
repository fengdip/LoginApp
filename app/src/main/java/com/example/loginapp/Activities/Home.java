package com.example.loginapp.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.loginapp.Fragments.HomeFragment;
import com.example.loginapp.Fragments.ProfileFragment;
import com.example.loginapp.Fragments.SettingsFragment;
import com.example.loginapp.Models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.loginapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private static final int PReqCode = 2 ;
    private static final int REQUESTCODE = 2;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    Dialog popAddPost;
    ImageView popupUserImage,popupPostImage,popupAddBtn;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;
    private Uri pickedImgUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //intialise
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //initialise popup
        iniPopup();
        setupPopupImageClick();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               popAddPost.show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_settings,
                R.id.nav_signout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //tags respective elements in layout xml file to onclick listener
        navigationView.setNavigationItemSelectedListener(this);
        updateNavHeader();

        //set up home fragment as default
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment , new HomeFragment()).commit();
    }

    private void setupPopupImageClick() {

        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click image to open gallery and allow permission

                checkAndRequestForPermission();

            }
        });

    }

    private void checkAndRequestForPermission() {

        if(ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(this, "Please grant the required permission ", Toast.LENGTH_SHORT).show();
            }
            else{
                ActivityCompat.requestPermissions(Home.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PReqCode);
            }

        }
        else{
            //everything goes well : have permission to access user gallery
            openGallery();
        }

    }

    private void openGallery() {
        //Open gallery and wait for user to pick an image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESTCODE);
    }

    //happens when user picks an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data !=null){

            //the user has successfully picked an image
            // we need to save its reference to a variable
            pickedImgUri =data.getData();
            popupPostImage.setImageURI(pickedImgUri);


        }
    }

    private void iniPopup() {//initialise the popout that we created
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        //initialise popup widgets
        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupPostImage = popAddPost.findViewById(R.id.popup_img);
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);

        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddBtn.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);

                //need to test all input fields(Title and description and image
                if (!popupTitle.getText().toString().isEmpty() && !popupDescription.getText().toString().isEmpty() && pickedImgUri !=null ){
                    //everything is ok

                    //upload post Image and access firebase storage
                    StorageReference storageRference = FirebaseStorage.getInstance().getReference().child("blog_images");
                    final StorageReference imageFilePath = storageRference.child(pickedImgUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageDownloadLink = uri.toString();
                                    //create post object
                                    Post post= new Post(popupTitle.getText().toString(),
                                            popupDescription.getText().toString(),
                                            imageDownloadLink,
                                            currentUser.getUid(),
                                            currentUser.getPhotoUrl().toString());

                                    //Add post to firebase database

                                    addPost(post);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //something goes wrong uploading the picture

                                    showMessage(e.getMessage());
                                    popupClickProgress.setVisibility(View.INVISIBLE);
                                    popupAddBtn.setVisibility(View.VISIBLE);
                                }
                            });

                        }
                    });


                }else{
                    if(popupTitle.getText().toString().isEmpty()){
                        showMessage("No Title!");
                        popupAddBtn.setVisibility(View.VISIBLE);
                        popupClickProgress.setVisibility(View.INVISIBLE);
                    }
                    if (popupDescription.getText().toString().isEmpty()){
                        showMessage(("No description!"));
                        popupAddBtn.setVisibility(View.VISIBLE);
                        popupClickProgress.setVisibility(View.INVISIBLE);
                    }
                    if (pickedImgUri == null){
                        showMessage("No image picked!");
                        popupAddBtn.setVisibility(View.VISIBLE);
                        popupClickProgress.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        //load user profile photo
        Glide.with(this).load(currentUser.getPhotoUrl()).into(popupUserImage);


    }

    private void addPost(Post post){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        post.setPostKey(key);

        //add post data to firebase database
        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                showMessage("Post Added successfully");
                popupClickProgress.setVisibility(View.INVISIBLE);
                popupAddBtn.setVisibility(View.VISIBLE);
                popAddPost.dismiss();

            }
        });


    }

    private void showMessage(String message) {
        Toast.makeText(Home.this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();

        if(id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void updateNavHeader() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.nav_username);
        TextView navUserMail = headerView.findViewById(R.id.nav_user_mail);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        navUserMail.setText("Mail : " + currentUser.getEmail());
        navUserName.setText("User : " + currentUser.getDisplayName());

        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhoto);


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            getSupportActionBar().setTitle("Home");
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();

        } else if (id == R.id.nav_profile) {

            getSupportActionBar().setTitle("Profile");
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ProfileFragment()).commit();

        } else if (id == R.id.nav_settings) {

            getSupportActionBar().setTitle("Settings");
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new SettingsFragment()).commit();

        }else if (id == R.id.nav_signout){

            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivity(loginActivity);
            finish();

        }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;


        }

}
