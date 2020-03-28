package com.example.snsproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.loginButton).setOnClickListener(onClickListener);
        findViewById(R.id.gotoPasswordResetButton).setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.loginButton:
                    login();
                    break;
                case R.id.gotoPasswordResetButton:
                    myStartActivity(PasswordResetActivity.class);
                    break;
            }
        }
    };

    private void login() {
        String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();

    if(email.length() > 0 && password.length() > 0) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            startToast("로그인에 성공했습니다.");
                            myStartActivity(MainActivity.class);
                        } else {
                            // If sign in fails, display a message to the user.
                            startToast(task.getException().toString());
                        }
                    }
                });
    }
    else { // 이메일, 비밀번호를 입력하지 않았을 때
        startToast("이메일 또는 비밀번호를 입력해주세요.");
    }
    }
    private void startToast(String msg) { // 리스너에서 사용하기 위해 함수 만듬.
        Toast.makeText(this, msg , Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c) {
        Intent intent=new Intent(this,c);
        // 메인 액티비티에서 뒤로가기 했을 때, 앱이 꺼지게 하기 위해 -> 이걸 사용하지 않으면 로그인창으로 뒤로가기됨.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}





