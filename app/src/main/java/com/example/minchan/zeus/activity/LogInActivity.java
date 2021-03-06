package com.example.minchan.zeus.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.minchan.zeus.R;
import com.example.minchan.zeus.service.EarthQuakeService;
import com.example.minchan.zeus.service.EmergencyNotificationService;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    SignInButton Google_Login;

    //google login result 상수
    private static final int RC_SIGN_IN = 1000;
    //firebase 인증 객체 생성
    private FirebaseAuth firebaseAuth;
    //google api 클라이언트
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();

        //google login 을 앱에 통합.
        //GoogleSignInOptions 개체를 구성할 때 resultIdToKen을 호출
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        Google_Login = findViewById(R.id.btn_googleSignIn);
        Google_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //google login버튼 응답
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                //구글 로그인 성공해서 firebase에 인증
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else{
                //구글 로그인 실패
                //Toast.makeText(LoginActivity.this, "로그인 인증 실패", Toast.LENGTH_SHORT).show();
                Toast.makeText(LoginActivity.this, "에러코드"+result.getStatus().getStatusCode(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(LoginActivity.this, "에러메세지"+result.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    //사용자가 정상적으로 로그인 한 후, GoogleSignInAccount 개체에서 ID토큰을 가져옴,
    //firebase 사용자 인증 정보로 교환 후, firebase 사용자 인증 정보를 사용해 firebase 에 인증.
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "로그인 인증 실패", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "로그인 인증 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onStart(){
        super.onStart();

        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            //user가 로그인 되어있는 경우
            Toast.makeText(LoginActivity.this, "이미 로그인 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            //user가 로그인 되어있지 않은 경우
            
        }

        startService(new Intent(LoginActivity.this, EmergencyNotificationService.class));
        startService(new Intent(LoginActivity.this, EarthQuakeService.class));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    // hashkey 얻는 문장. terminal에서 실행
    //keytool -exportcert -alias androiddebugkey -keystore "C:\Users\ImJunhyuk\.android\debug.keystore" | "C:\openssl-0.9.8k_X64\bin\openssl" sha1 -binary | "C:\openssl-0.9.8k_X64\bin\openssl" base64
}
