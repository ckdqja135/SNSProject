package com.example.snsproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) { // 로그인이 안됐을 때 -> 로그인 여부 확인하는 부분
            myStartActivity(SignUpActivity.class);
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("cities").document("SF");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document != null) { // document가 null 일 때
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else { // document의 데이터가 없으면 MemberActivity를 실행.
                                Log.d(TAG, "No such document");
                                myStartActivity(MemberActivity.class);
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }

        findViewById(R.id.logoutButton).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.logoutButton:
                    FirebaseAuth.getInstance().signOut();
                    startSignUpActivity();
                    break;
            }
        }
    };

    private void startSignUpActivity() {
        Intent intent=new Intent(this,SignUpActivity.class);
        startActivity(intent);
    }
    private void myStartActivity(Class c) {
        Intent intent=new Intent(this,c);
        // 메인 액티비티에서 뒤로가기 했을 때, 앱이 꺼지게 하기 위해 -> 이걸 사용하지 않으면 로그인창으로 뒤로가기됨.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
