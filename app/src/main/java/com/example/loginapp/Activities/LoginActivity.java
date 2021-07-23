package com.example.loginapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.loginapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText userMail,userPassword;
    private Button btnLogin,signUpBtn;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private Intent PostLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userMail = findViewById(R.id.login_mail);
        userPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.loginBtn);
        signUpBtn = findViewById(R.id.signUpBtn);
        loginProgress = findViewById(R.id.login_progress);
        mAuth = FirebaseAuth.getInstance();
        PostLogin = new Intent(this, com.example.loginapp.Activities.Home.class);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerActivity = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(registerActivity);
                finish();
            }
        });

        loginProgress.setVisibility(View.INVISIBLE);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                loginProgress.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                final String mail = userMail.getText().toString();
                final String password = userPassword.getText().toString();
                
                if (mail.isEmpty()){
                    showMessage("Mail is empty!");
                    loginProgress.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                }
                if(password.isEmpty()){
                    showMessage("Password is empty!");
                    loginProgress.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                }
                else{
                    signIn(mail,password);
                }
            }
        });
    }

    private void signIn(String mail, String password) {

        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                
                if (task.isSuccessful()){
                    
                    loginProgress.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                    updateUI();
                }
                else{

                    showMessage(task.getException().getMessage());
                    loginProgress.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                }
                
            }
        });
    }

    private void updateUI() {

        startActivity(PostLogin);
        finish();

    }


    private void showMessage(String text) {

        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            //user is already connected hence direct him to postlogin page

            updateUI();

        }
    }

}
