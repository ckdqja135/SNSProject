package com.example.snsproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MemberActivity extends AppCompatActivity {
    private static final String TAG = "MemberinitActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);
        findViewById(R.id.checkbutton).setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.checkbutton:
                    profileUpdate();
                    break;
            }
        }
    };

    private void profileUpdate() {
        String name = ((EditText) findViewById(R.id.nameEditText)).getText().toString();
        String phoneNumber = ((EditText) findViewById(R.id.phoneNumberEditText)).getText().toString();

        String birthDay = ((EditText) findViewById(R.id.birthDayEditText)).getText().toString();
        String address = ((EditText) findViewById(R.id.addressEditText)).getText().toString();

         if(name.length() > 0 && phoneNumber.length() > 9 && birthDay.length() > 5 && address.length() > 0) { // 이 조건에 통과해야 정상 등록됨.
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            MemberInfo memberInfo = new MemberInfo(name, phoneNumber, birthDay, address);
             if(user != null) { // null 처리 -> main에서 처리해서 null 가능성이 없지만 처리시켜준다.
                 db.collection("users").document(user.getUid()).set(memberInfo)
                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             startToast("회원정보 등록을 성공하였습니다.");
                             finish();
                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             startToast("회원정보 등록에 실패하였습니다.");
                             Log.w(TAG, "Error adding document", e);
                         }
                     });
             }
         } else { // 회원정보를 입력하지 않았을 때
            startToast("회원정보를 입력해주세요.");
        }
    }
    private void startToast(String msg) { // 리스너에서 사용하기 위해 함수 만듬.
        Toast.makeText(this, msg , Toast.LENGTH_SHORT).show();
    }
}





