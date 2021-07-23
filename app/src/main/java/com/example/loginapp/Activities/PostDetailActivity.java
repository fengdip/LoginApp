package com.example.loginapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.loginapp.Models.Comment;
import com.example.loginapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    ImageView imgPost,imgUserPost,imgCurrentUser;
    TextView txtPostDesc,txtPostDateName,txtPostTitle;
    EditText editTextComment;
    Button btnAddComment;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String PostKey;
    FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //set statue bar to transparent

        Window w =getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getSupportActionBar().hide();

        //initialise view

        imgPost = findViewById(R.id.post_detail_img);
        imgUserPost = findViewById(R.id.post_detail__user_img);
        imgCurrentUser = findViewById(R.id.post_detail_currentuser_img);

        txtPostTitle = findViewById(R.id.post_detail_title);
        txtPostDesc = findViewById(R.id.post_detail_desc);
        txtPostDateName = findViewById(R.id.post_detail_date_name);

        editTextComment = findViewById(R.id.post_detail_comment);
        btnAddComment = findViewById(R.id.post_detail_add_comment_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        
        //add Comment button click listener
        
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference commentReference = firebaseDatabase.getReference("Comment").child(PostKey)
                    .push();
                String comment_content = editTextComment.getText().toString();
                
                btnAddComment.setVisibility(View.INVISIBLE);
                String uid = firebaseUser.getUid();
                String uname = firebaseUser.getDisplayName();
                String uimg = firebaseUser.getPhotoUrl().toString();
                Comment comment = new Comment(comment_content,uid,uimg,uname);
                
                commentReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("comment added");
                        editTextComment.setText("");
                        btnAddComment.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("fail to add comment : "+e.getMessage());
                    }
                });
            }
        });

      /* binding data to views
        1st retrieve data from firebase
        then display on views
        by retrieving data from adapter*/

      String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(imgPost);

        String postTitle = getIntent().getExtras().getString("title");
        txtPostTitle.setText(postTitle);

        String userpostImage = getIntent().getExtras().getString("userPhoto");
        Glide.with(this).load(userpostImage).into(imgUserPost);

        String postDescription = getIntent().getExtras().getString("description");
        txtPostDesc.setText(postDescription);

        //set user image
        Glide.with(this).load(firebaseUser.getPhotoUrl()).into(imgCurrentUser);

        //get post ID

        PostKey = getIntent().getExtras().getString("postKey");

        String date = timestampToString(getIntent().getExtras().getLong("postDate"));
        txtPostDateName.setText(date);

    }

    private void showMessage(String message) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String timestampToString(long time){

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        Date date1 = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");



        String date = format.format(date1);
        return date;
    }
}
