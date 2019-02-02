package com.example.minwoo.pingpongchat_client;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class UserProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        TextView tvPersonName;
        TextView tvPersonEmail;
        ImageView ivPersonPhoto;

        tvPersonName = (TextView)findViewById(R.id.tvPersonName);
        tvPersonEmail = (TextView)findViewById(R.id.tvPersonEmail);
        ivPersonPhoto = (ImageView)findViewById(R.id.ivPersonPhoto);

        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String personPhotoUrl = acct.getPhotoUrl().toString();

            tvPersonName.setText(personName);
            tvPersonEmail.setText(personEmail);
            Glide.with(this)
                 .load(personPhotoUrl)
                 .into(ivPersonPhoto);
        } else {
            Toast.makeText(UserProfileActivity.this, "정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
