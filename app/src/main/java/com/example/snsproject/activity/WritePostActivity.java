package com.example.snsproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.snsproject.R;
import com.example.snsproject.WriteInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class WritePostActivity extends BasicActivity {
    private static final String TAG = "WritePostActivity";
    private FirebaseUser user;
    private ArrayList<String> pathList = new ArrayList<>();
    private LinearLayout parent;
    private RelativeLayout buttonsBackgroundLayout;
    private ImageView selectedImageView;
    private EditText selectedEditText;
    private int pathCount, successCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        parent = findViewById(R.id.contentLayout);

        buttonsBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);
        buttonsBackgroundLayout.setOnClickListener(onClickListener);
        findViewById(R.id.check).setOnClickListener(onClickListener);
        findViewById(R.id.image).setOnClickListener(onClickListener);
        findViewById(R.id.video).setOnClickListener(onClickListener);
        findViewById(R.id.imageModify).setOnClickListener(onClickListener);
        findViewById(R.id.videoModify).setOnClickListener(onClickListener);
        findViewById(R.id.delete).setOnClickListener(onClickListener);
        findViewById(R.id.contentEditText).setOnFocusChangeListener(onFocusChangeListener);
        findViewById(R.id.titleeditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    selectedEditText = null;
                }
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0 :
                if (resultCode == Activity.RESULT_OK) {
                    String profilePath = data.getStringExtra("profilePath");
                    pathList.add(profilePath);

                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    if(selectedEditText == null) { // null일 때는 평소처럼 아래로 추가 되게함.
                        parent.addView(linearLayout);
                    } else {
                        for (int i = 0; i < parent.getChildCount(); i++) { // 현재 selectedEdit위치에서 그 다음에 만든다는 의미.
                            if (parent.getChildAt(i) == selectedEditText.getParent()) {
                                parent.addView(linearLayout, i + 1);
                                break;
                            }
                        }
                    }

                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                            selectedImageView = (ImageView) v;
                        }
                    });
                    Glide.with(this).load(profilePath).override(500).into(imageView); // 1000사이즈의 이미지가 들어옴.
                    linearLayout.addView(imageView);

                    EditText editText = new EditText(WritePostActivity.this);
                    editText.setLayoutParams(layoutParams);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    editText.setHint("내용");
                    editText.setOnFocusChangeListener(onFocusChangeListener);
                    linearLayout.addView(editText);
                }
                break;
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    String profilePath = data.getStringExtra("profilePath");
                    Glide.with(this).load(profilePath).override(500).into(selectedImageView); // 1000사이즈의 이미지가 들어옴.
                }
                break;
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.check:
                    storageUpload();
                break;
                case R.id.image:
                    myStartActivity(GallaryActivity.class, "image", 0);
                break;
                case R.id.video:
                    myStartActivity(GallaryActivity.class, "video", 0);
                break;
                case R.id.buttonsBackgroundLayout:
                    if(buttonsBackgroundLayout.getVisibility() == View.VISIBLE) {
                        buttonsBackgroundLayout.setVisibility(View.GONE);
                    }
                break;
                case R.id.imageModify:
                    myStartActivity(GallaryActivity.class, "image", 1);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                break;
                case R.id.videoModify:
                    myStartActivity(GallaryActivity.class, "video", 1);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                break;
                case R.id.delete:
                    parent.removeView((View)selectedImageView.getParent());
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                break;
            }
        }
    };

    private void storageUpload() {
        final String title = ((EditText) findViewById(R.id.titleeditText)).getText().toString();
        if (title.length() > 0) { // 이 조건에 통과해야 정상 등록됨.
            final ArrayList<String> contentList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser(); // DB 초기화
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
           final DocumentReference documentReference = firebaseFirestore.collection("posts").document();

            for(int i = 0; i < parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout)parent.getChildAt(i);

                for(int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);
                    if(view instanceof EditText) { // EditText일 때
                        String text = ((EditText)view).getText().toString();
                        if (text.length() > 0) {
                            contentList.add(text);
                        }
                    } else {
                        contentList.add(pathList.get(pathCount)); // 이거 덕에 contentList.set()사용 가능.
                        final StorageReference mountainsRef = storageRef.child("posts/"+ documentReference.getId() +"/"+pathCount+".jpg");
                        try {
                            InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                            // Create file metadata including the content type
                            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", ""+ (contentList.size()-1)).build();
                            UploadTask uploadTask = mountainsRef.putStream(stream, metadata);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                                    mountainsRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.e("로그 :", "uri :"+ uri);
                                            contentList.set(index, uri.toString());
                                            successCount++;
                                            if(pathList.size() == successCount) {
                                                // 완료
                                                WriteInfo writeInfo = new WriteInfo(title, contentList, user.getUid(), new Date());
                                                storeupload(documentReference, writeInfo);
                                                for(int a = 0; a < contentList.size(); a++) {
                                                    Log.e("로그", "콘텐츠 :"+ contentList.get(a));
                                                }
                                            }
                                        }
                                    });
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                    // ...
                                }
                            });
                        } catch (FileNotFoundException e) {
                            Log.e("로그", "에러 : " + e.toString());
                        }
                        pathCount++;
                    }
                }
            }
        } else {
            startToast("제목을 입력해 주세요.");
        }
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                selectedEditText = (EditText) v;
            }
        }
    };

    private void storeupload(DocumentReference documentReference, WriteInfo writeInfo) {
        documentReference.set(writeInfo)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    finish();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document", e);
                }
            });
    }
    private void startToast(String msg) { // 리스너에서 사용하기 위해 함수 만듬.
        Toast.makeText(this, msg , Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c, String media, int requestCode) {
        Intent intent=new Intent(this,c);
        intent.putExtra("media", media);
        startActivityForResult(intent, requestCode);
    }
}


