package com.example.snsproject;

import android.content.Intent;
import android.os.Bundle;
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

public class SignUpActivity  extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "SignUpActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.signUpButton).setOnClickListener(onClickListener);
        findViewById(R.id.gotoLoginButton).setOnClickListener(onClickListener);
    }


    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override public void onBackPressed() { // 뒤로가기 시 앱이 종료하는 이벤트
            super.onBackPressed();
            // 앱을 종료 시켜버림.
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.signUpButton:
                    signUp();
                    break;
                case R.id.gotoLoginButton:
                    startLoginActivity();
                    break;
            }
        }
    };

    private void signUp() {
        String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
        String passwordCheck = ((EditText) findViewById(R.id.passwordCheckEditText)).getText().toString();

    if(email.length() > 0 && password.length() > 0 && passwordCheck.length() > 0) {
        if (password.equals(passwordCheck)) { // 비밀번호 체크
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                startToast("회원가입에 성공했습니다.");
                            } else {
                                if (task.getException() != null) { // null처리
                                    startToast(task.getException().toString());
                                }
                            }
                        }
                    });
        } else { // 비밀번호입력란과 비밀번호 확인값이 다를 때
            startToast("비밀번호가 일치하지 않습니다.");
        }
    }
        else { // 이메일, 비밀번호를 입력하지 않았을 때
            startToast("이메일 또는 비밀번호를 입력해주세요.");
        }
    }

    private void startToast(String msg) { // 리스너에서 사용하기 위해 함수 만듬.
        Toast.makeText(this, msg , Toast.LENGTH_SHORT).show();
    }

    private void startLoginActivity() {
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}





