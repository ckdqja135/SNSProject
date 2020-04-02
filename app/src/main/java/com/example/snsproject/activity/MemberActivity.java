package com.example.snsproject.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.snsproject.MemberInfo;
import com.example.snsproject.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MemberActivity extends AppCompatActivity {
    private static final String TAG = "MemberinitActivity";
    private ImageView profileImageView;
    private String profilePath;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);

        profileImageView = findViewById(R.id.profileimageView);
        profileImageView.setOnClickListener(onClickListener);

        findViewById(R.id.checkbutton).setOnClickListener(onClickListener);
        findViewById(R.id.picture).setOnClickListener(onClickListener);
        findViewById(R.id.gallary).setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case 0 : {
                    if (resultCode == Activity.RESULT_OK) {
                        profilePath = data.getStringExtra("profilePath");
                        Log.e("로그", "profilePath :" + profilePath);
                        Bitmap bmp = BitmapFactory.decodeFile(profilePath);
                        profileImageView.setImageBitmap(bmp);
                    }
                    break;
                }
            }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.checkbutton:
                    profileUpdate();
                    break;
                case R.id.profileimageView:
                    CardView cardView = findViewById(R.id.buttonsCardView);
                    if(cardView.getVisibility() == View.VISIBLE) // cardview가 보이면 안보이게
                        cardView.setVisibility(View.GONE);
                    else { // cardview가 안보이면 보이게 함
                        cardView.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.picture:
                    myStartActivity(CameraActivity.class);
                    break;
                case R.id.gallary:
                    if (ContextCompat.checkSelfPermission(MemberActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 권한을 다시 묻는 곳
                        ActivityCompat.requestPermissions(MemberActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MemberActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        } else {
                            startToast("권한을 허용해 주세요.");
                        }
                    } else {
                        // 권한을 허용 했을 때
                        myStartActivity(GallaryActivity.class);
                    }

                    break;
            }
        }
    };
    // 권한 요청 응답 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // 권한 주었을 때
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myStartActivity(GallaryActivity.class);
                } else {
                    // 안 주었을 때
                   startToast("권한을 허용해 주세요.");
                }
            }
        }
    }

    private void profileUpdate() {
        final String name = ((EditText) findViewById(R.id.nameEditText)).getText().toString();
        final String phoneNumber = ((EditText) findViewById(R.id.phoneNumberEditText)).getText().toString();
        final String birthDay = ((EditText) findViewById(R.id.birthDayEditText)).getText().toString();
        final String address = ((EditText) findViewById(R.id.addressEditText)).getText().toString();

         if(name.length() > 0 && phoneNumber.length() > 9 && birthDay.length() > 5 && address.length() > 0) { // 이 조건에 통과해야 정상 등록됨.

             FirebaseStorage storage = FirebaseStorage.getInstance();
             // Create a storage reference from our app
             StorageReference storageRef = storage.getReference();
             // Create a reference to "mountains.jpg"
             user = FirebaseAuth.getInstance().getCurrentUser(); // DB 초기화
             final StorageReference mountainsRef = storageRef.child("users/"+ user.getUid() +"/profilesimage.jpg");

             //  null이면 정보만 보내고, 아니면 사진도 같이 보내라는 뜻.
             if(profilePath == null) {
                 MemberInfo memberInfo = new MemberInfo(name, phoneNumber, birthDay, address);
                 Uploader(memberInfo);
             }else {
                 try {
                     InputStream stream = new FileInputStream(new File(profilePath));
                     UploadTask uploadTask = mountainsRef.putStream(stream);
                     uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                         @Override
                         public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                             if (!task.isSuccessful()) {
                                 throw task.getException();
                             }
                             return mountainsRef.getDownloadUrl();
                         }
                     }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                         @Override
                         public void onComplete(@NonNull Task<Uri> task) {
                             if (task.isSuccessful()) {
                                 Uri downloadUri = task.getResult();
                                 MemberInfo memberInfo = new MemberInfo(name, phoneNumber, birthDay, address, downloadUri.toString());
                                 Uploader(memberInfo);
                             } else {
                                 Log.e("로그", "회원정보를 보내는데 실패하였습니다.");
                             }
                         }
                     });
                 } catch (FileNotFoundException e) {
                     Log.e("로그", "에러 : " + e.toString());
                 }
             }
         } else { // 회원정보를 입력하지 않았을 때
            startToast("회원정보를 입력해주세요.");
        }
    }

    private void Uploader(MemberInfo memberInfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    private void startToast(String msg) { // 리스너에서 사용하기 위해 함수 만듬.
        Toast.makeText(this, msg , Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c) {
        Intent intent=new Intent(this,c);
        startActivityForResult(intent, 0);
    }
}





