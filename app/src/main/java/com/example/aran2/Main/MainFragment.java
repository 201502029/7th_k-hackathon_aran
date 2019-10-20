package com.example.aran2.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.aran2.InfoBean;
import com.example.aran2.Login.LoginActivity;
import com.example.aran2.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static java.lang.System.out;

public class MainFragment extends Fragment {
    Button button;
    View v;
    ImageView iv_babyPhoto;
    TextView tv_name, tv_age, tv_signUpDate;
    final int RECEIVE_PHOTO = 11;
    final int IMAGE_CROP = 12;
    final int IMAGE_SAVE = 13;
    String uid;
    Uri imageUri;
    InfoBean infoBean;
    boolean isLoad;
    StorageReference storageReference;
    Button logOutButton;
    String Realuri;
    Long signUpLong;
    String name;
    Integer age;
    private Typeface typeface;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main, container, false);
        if(typeface == null) {
            typeface = Typeface.createFromAsset(getActivity().getAssets(),
                    "BinggraeMelona.ttf");
        }
        setGlobalFont((ViewGroup) v);
        iv_babyPhoto = v.findViewById(R.id.main_babyImage);
        tv_name = v.findViewById(R.id.main_babyName);
        uid = "id";

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        tv_age = v.findViewById(R.id.main_babyAge);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GradientDrawable backGround = (GradientDrawable) getContext().getDrawable(R.drawable.image_round);
            iv_babyPhoto.setBackground(backGround);
            iv_babyPhoto.setClipToOutline(true);
        }
        tv_signUpDate = v.findViewById(R.id.main_signUpDate);
        iv_babyPhoto.getLayoutParams().width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.5);
        iv_babyPhoto.getLayoutParams().height = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.5);


        button = v.findViewById(R.id.main_selectPhoto);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, RECEIVE_PHOTO);
            }
        });
        iv_babyPhoto.setImageResource(R.drawable.default_user);

        if (isLoad != true) {
            storageReference = FirebaseStorage.getInstance().getReference("user/" + uid);

            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (getContext() != null) {
                        Realuri = uri.toString();
                        Glide.with(getContext()).asBitmap()
                                .load(uri.toString()).apply(new RequestOptions().circleCrop().centerCrop()).into(iv_babyPhoto);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    iv_babyPhoto.setImageResource(R.drawable.default_user);
                }
            });
            getString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!isLoad) {
                    }
                    Long temp = System.currentTimeMillis() - signUpLong;
                    SimpleDateFormat day = new SimpleDateFormat("dddd");
                    final String result = day.format(new Date(temp));
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_name.setText(name);
                                tv_signUpDate.setText(result + "일 째");
                                tv_age.setText(String.valueOf(age) + "살");

                            }
                        });
                    }
                }
            }).start();

        }else{
            Long temp = System.currentTimeMillis() - signUpLong;
            SimpleDateFormat day = new SimpleDateFormat("dddd");
            final String result = day.format(new Date(temp));

            tv_name.setText(name);
            tv_signUpDate.setText(result + "일 째");
            tv_age.setText(String.valueOf(age) + "살");
            if (getContext() != null) {
                Glide.with(getContext()).asBitmap()
                        .load(Realuri).apply(new RequestOptions().circleCrop().centerCrop()).into(iv_babyPhoto);
            }
        }

        return v;
    }
    void setGlobalFont(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof TextView)
                ((TextView)child).setTypeface(typeface);
            else if (child instanceof ViewGroup)
                setGlobalFont((ViewGroup)child);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RECEIVE_PHOTO:
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    imageUri =data.getData();
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("outputX", 100);
                    intent.putExtra("outputY", 100);
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("scale", true);
                    intent.putExtra("return-data", true);
                    intent.putExtra("output", imageUri);
                    startActivityForResult(intent, IMAGE_CROP);
                    break;
                case IMAGE_CROP:
                    Intent intent1 = new Intent(getContext() , SelectPhotoActivity.class);
                    intent1.putExtra("photoUri", imageUri);
                    startActivityForResult(intent1, IMAGE_SAVE);
                    break;
                case IMAGE_SAVE:
                    storageReference = FirebaseStorage.getInstance().getReference("user/" + uid);
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (getContext() != null) {
                                Realuri = uri.toString();
                                Glide.with(getContext()).asBitmap()
                                        .load(uri.toString()).apply(new RequestOptions().circleCrop().centerCrop()).into(iv_babyPhoto);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            iv_babyPhoto.setImageResource(R.drawable.default_user);
                        }
                    });
                    break;
            }
        }
    }

    public void getString() {
        uid = "id";

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(uid + "/userInfo");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                infoBean = dataSnapshot.getValue(InfoBean.class);
                name = infoBean.getName();
                String[] year = infoBean.getBirth().split("-");
                int a = Integer.valueOf(year[0]);
                Date date = new Date();
                SimpleDateFormat day2 = new SimpleDateFormat("yyyy-MM-dd");
                String signUpDate = day2.format(date);
                String[] d = signUpDate.split("-");
                int b = Integer.valueOf((d[0]));
                age = b - a + 1;
                signUpLong = infoBean.getDay();
                isLoad = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}