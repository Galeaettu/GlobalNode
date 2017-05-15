package a100588.galea.christian.globalnodes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountDetails extends AppCompatActivity {

    private static final int RC_SIGN_IN = 0;
    private FirebaseAuth auth;

    private ImageView userImage;
    private ImageView userCover;
    private TextView userName;
    private TextView userGender;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private NavigationView navigationView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);
        FacebookSdk.sdkInitialize(getApplicationContext());

        mDrawerLayout = (DrawerLayout)findViewById(R.id.account_details_drawer_layout);
        mActivityTitle = getTitle().toString();

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            //user already signed in
            Log.d("AUTH ACTIVITY", auth.getCurrentUser().getEmail());
        }else{
            finish();
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.login_logo)
                    .setProviders(
                            AuthUI.FACEBOOK_PROVIDER)
                    .build(), RC_SIGN_IN);

        }
        setUserDetails();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        NavigationHelper nh = new NavigationHelper();
        nh.setHeaderDetails(auth,navigationView, getApplicationContext());

        mToolbar = (Toolbar)findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.nav_account){
                    openUserDetails(getCurrentFocus());
                    Log.d("NAV", "ACCOUNT");
                }

                if (id == R.id.nav_chat){
                    openChatActivity(getCurrentFocus());
                    Log.d("NAV", "CHAT");
                }

                if(id == R.id.nav_log_out){
                    auth.signOut();
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setTheme(R.style.LoginTheme)
                            .setLogo(R.drawable.login_logo)
                            .setProviders(
                                    AuthUI.FACEBOOK_PROVIDER)
                            .build(), RC_SIGN_IN);
                    Log.d("NAV", "SIGN OUT");
                }

                if(id == R.id.nav_close_app){
                    finish();
                }

                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        navigationView.getMenu().getItem(0).setChecked(true);


        // FACEBOOK TEST

        final ProgressDialog mProgress = new ProgressDialog(AccountDetails.this);
        mProgress.setMessage("Loading Account...");
        mProgress.setTitle(R.string.my_account);
        mProgress.show();

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken()
                , new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mProgress.show();
                        String user_id = "";
                        String user_name = "";
                        String user_gender = "";
                        JSONObject cover;
                        JSONObject picture;
                        String user_cover = "";
                        String user_picture = "";
                        try {
//                            Log.d("FB TEST",object.toString() );
                            user_id = object.getString("id");
                            user_name = object.getString("name");
                            user_gender = object.getString("gender");
                            cover = object.getJSONObject("cover");
                            user_cover = cover.getString("source");
                            picture = object.getJSONObject("picture").getJSONObject("data");
                            user_picture = picture.getString("url");

                            userName.setText(user_name);
                            userGender.setText(user_gender);

                            Picasso.with(getApplicationContext()).load(user_picture)
                                    .transform(new RoundedTransformation())
                                    .into(userImage);
                            Picasso.with(getApplicationContext()).load(user_cover)
                                    .fit()
                                    .centerCrop()
                                    .into(userCover);

                        }catch(Exception e){
                            Log.d("FACEBOOK ERR","ERR");
                        }
                        //response.getJSONArray();
                        Log.d("FB TEST",user_id );
                        Log.d("FB TEST",user_name );
                        Log.d("FB TEST",user_gender );
                        Log.d("FB TEST COVER",user_cover );
                        Log.d("FB TEST",user_picture );
                        Log.d("FB TEST",object.toString() );
                        Log.d("FB COMPLETE", "COMPLETE");
                        mProgress.dismiss();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,gender,location,cover,picture.width(500).height(500)");
        request.setParameters(parameters);
        request.executeAsync();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                //User logged in
                Log.d("AUTH", auth.getCurrentUser().getEmail());
                Log.d("AUTH", auth.getCurrentUser().getDisplayName());
                Log.d("AUTH", auth.getCurrentUser().getPhotoUrl().toString());
                Log.d("AUTH", AccessToken.getCurrentAccessToken().getUserId());

            }
            else{
                //User not authenticated
                Log.d("AUTH", "NOT AUTHENTICATED");
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("AUTH", "USER LOGGED OUT");
                                finish();
                            }
                        });
            }
        }
    }

    private void setUserDetails(){
        userImage = (ImageView) findViewById(R.id.userImage);
        userName = (TextView) findViewById(R.id.userName);
        userCover = (ImageView)findViewById(R.id.userCover);
        userGender = (TextView)findViewById(R.id.userGender);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //
//
//
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_exit) {
            moveTaskToBack(true);
            finish();
            return true;
        }

        if (id == R.id.menu_log_out) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("AUTH", "USER LOGGED OUT");
                            finish();
                        }
                    });
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.login_logo)
                    .setProviders(
                            AuthUI.FACEBOOK_PROVIDER)
                    .build(), RC_SIGN_IN);
            Log.d("NAV", "SIGN OUT");
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void openUserDetails(View view){
        Intent intent = new Intent(this,AccountDetails.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME );
        startActivity(intent);
    }
    public void openChatActivity(View view){
        Intent intent = new Intent(this,ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME );
        startActivity(intent);
    }
}
