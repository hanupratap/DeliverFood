package com.example.deliverfood;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AlertDialog;;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.grpc.okhttp.internal.framed.Header;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LoginButton loginButton;
    TextView username;
    private ProgressDialog dialog;
    ImageView profileImg;

    ConstraintLayout constraintLayout;

//    private boolean first_user = false;
    private CardView cardView;
    private ProgressDialog progressDialog;

    void requestLocation()
    {
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            
            new AlertDialog.Builder(this)
                    .setTitle("Location Services Required")
                    .setMessage("GPS not enabled")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel",null)
                    .show();
        }
    }

    private static final int RC_SIGN_IN = 1;
    private static final String TAG =  "MyActivity" ;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
        return;
    }


    String image_url;

    CoordinatorLayout linear;

    TextView lastlogin;

    LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardView = findViewById(R.id.user_card);
        linear = findViewById(R.id.linearLayout1);
        lastlogin = findViewById(R.id.lastlogin);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);


        constraintLayout = findViewById(R.id.constraint);

        username = findViewById(R.id.userName);

        mAuth = FirebaseAuth.getInstance();

        profileImg = findViewById(R.id.profileImg);


        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(0,0,0,0)));

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginButton= findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);




                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });




        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();





        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        findViewById(R.id.signInButton).setOnClickListener(this);

        findViewById(R.id.orderbtn).setOnClickListener(this);


    }






    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        image_url = "https://graph.facebook.com/" + token.getUserId() + "/picture?type=large";

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        try {

            if(mAuth.getCurrentUser()!=null)
            {
                mAuth.getCurrentUser().linkWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "linkWithCredential:success");
                                    LoginManager.getInstance().logOut();

                                } else {
                                    Log.w(TAG, "linkWithCredential:failure or Account already linked!", task.getException());
                                    LoginManager.getInstance().logOut();

                                }

                                // ...
                            }
                        });
            }
            else
            {

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                progressDialog.setCancelable(false);

                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
//                                    first_user = task.getResult().getAdditionalUserInfo().isNewUser();
                                    Map map = new HashMap();
                                    map.put("profile_img", image_url);
                                    map.put("user_name", user.getDisplayName());
                                    FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(map,SetOptions.merge());
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithCredential:failure", task.getException());

                                    Snackbar.make(linear, "Authentication failed. ---", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Close", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                }
                                            })
                                            .setActionTextColor(getResources().getColor(R.color.colorAccent)).show();
                                    updateUI(null);
                                }

                                // ...
                            }
                        });
            }

        }
        catch (Exception e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();


        }

    }





    private void updateUI(FirebaseUser user) {

        if(user!=null)
        {


            FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.getString("last_login")!=null)
                    {
                        lastlogin.setText("Last login : " + documentSnapshot.getString("last_login"));
                    }

                    else
                    {
                        SpannableString sp = new SpannableString("Welcome");
                        sp.setSpan(new TypefaceSpan(Typeface.DEFAULT_BOLD), 0,"Welcome".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sp.setSpan(new ForegroundColorSpan(Color.argb(215,0,0,0)),0,"Welcome".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        lastlogin.setText(sp);
                    }
                    Map map = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    Date dt = new Date();
                    String S = sdf.format(dt); // formats to 09/23/2009 13:53:28.238
                    map.put("last_login", S);
                    FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map, SetOptions.merge());


                }
            });


            lottieAnimationView.setVisibility(View.VISIBLE);

            findViewById(R.id.signInButton).setVisibility(View.GONE);
            findViewById(R.id.orderbtn).setVisibility(View.VISIBLE);
            findViewById(R.id.login_button).setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            invalidateOptionsMenu();


            username.setText(user.getDisplayName());
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Glide.with(MainActivity.this)
                            .load(documentSnapshot.getString("profile_img"))
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImg);
                }
            });






        }
        else
        {

            lottieAnimationView.setVisibility(View.GONE);
            findViewById(R.id.signInButton).setVisibility(View.VISIBLE);
            findViewById(R.id.orderbtn).setVisibility(View.INVISIBLE);
            findViewById(R.id.login_button).setVisibility(View.VISIBLE);
            cardView.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }


        if(progressDialog!=null)
        if(progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }


    }



    private void signOut() {
        // Firebase sign out

            mAuth.signOut();



        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });

        LoginManager.getInstance().logOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        requestLocation();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    public static boolean isPackageInstalled(Context c, String targetPackage) {
        PackageManager pm = c.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            image_url = user.getPhotoUrl().toString();
                            Map map = new HashMap();
                            map.put("profile_img", image_url);
                            map.put("user_name", user.getDisplayName());
                            FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(map,SetOptions.merge());
                            if(isPackageInstalled(MainActivity.this, "com.facebook.katana")==true)
                            {
                                loginButton.performClick();
                            }





                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(linear, "Authentication Failed.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Close", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    })
                                    .setActionTextColor(getResources().getColor(R.color.colorAccent)).show();

                            updateUI(null);
                        }

                        // ...
                    }
                });


        if(mAuth.getCurrentUser()!=null)
        {
            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "linkWithCredential:success");
                                FirebaseUser user = task.getResult().getUser();
//                                first_user = task.getResult().getAdditionalUserInfo().isNewUser();

                                updateUI(user);
                            } else {
                                Log.w(TAG, "linkWithCredential:failure", task.getException());
                                Snackbar.make(linear, "Authentication Failed.", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Close", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                            }
                                        })
                                        .setActionTextColor(getResources().getColor(R.color.colorAccent)).show();
                                updateUI(null);
                            }

                            // ...
                        }
                    });
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.auth_menu, menu);
        if(mAuth.getCurrentUser()!=null)
        {
            menu.findItem(R.id.user_info).setTitle(mAuth.getCurrentUser().getDisplayName());
        }

        else
        {
            menu.findItem(R.id.user_info).setTitle("No User logged in");
            menu.findItem(R.id.user_info).setEnabled(false);

        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.logout:
            {
                signOut();
                return super.onOptionsItemSelected(item);

            }
            case R.id.Home:
            {
                Snackbar.make(linear, "Already on Home", Snackbar.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);

            }


        }
        return super.onOptionsItemSelected(item);
    }

    private void already_logged_in()
    {
        Intent a = new Intent(MainActivity.this, DeliverOrOrder.class);
        FirebaseUser user = mAuth.getCurrentUser();
        a.putExtra("user", user);
        MainActivity.this.startActivity(a);

    }

    private void signIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }




    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signInButton) {

            signIn();


        }
        else if(i == R.id.orderbtn)
        {
            already_logged_in();
        }
    }
}
