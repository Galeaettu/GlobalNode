package a100588.galea.christian.globalnodes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.provider.IDPProviderParcel;
import com.firebase.ui.auth.provider.IDPResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 0;
    public static final String PREFS_NAME = "TimeElapsed";
    private FirebaseAuth auth;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private TextView timeElapsedView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
//        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);

//        mDrawerLayout.addDrawerListener(mDrawerToggle);
//        mDrawerToggle.syncState();

        mActivityTitle = getTitle().toString();

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            //user already signed in
            Log.d("AUTH", auth.getCurrentUser().getEmail());
        }else{
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.icon_svg)
                    .setProviders(
                            AuthUI.FACEBOOK_PROVIDER)
                    .build(), RC_SIGN_IN);
        }

//        mToolbar = (Toolbar)findViewById(R.id.nav_action);
//        NavigationHelper navigationHelper = new NavigationHelper();
//        navigationHelper.setupDrawer(mToolbar);

        setupDrawer();
        timeSharedPreferences();
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView;
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.getMenu().getItem(1).setChecked(false);
        navigationView.getMenu().getItem(2).setChecked(false);


        timeSharedPreferences();
    }

    private void timeSharedPreferences() {
        long defaultValue = 0;

        timeElapsedView = (TextView)findViewById(R.id.home_chat_time);
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long shared_elapsed_time = sharedPref.getLong(getString(R.string.saved_default_time), defaultValue);
        Log.d("MAIN - STRING curr", Long.toString(shared_elapsed_time));

//        long difference = shared_elapsed_time - shared_elapsed_time_new;
        long difference = shared_elapsed_time;
        long differenceInSeconds = difference / DateUtils.SECOND_IN_MILLIS;
        String formatted = DateUtils.formatElapsedTime(differenceInSeconds);


        try{
//            timeElapsedView.setText(String.format(Locale.ENGLISH,"%d",shared_elapsed_time));
            timeElapsedView.setText(formatted);
        }catch(Exception e){
            Log.d("SHARED FAILED", e.getMessage());
        }
        Log.d("MAIN - STRING CREATE", formatted);
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

//                mToolbar = (Toolbar)findViewById(R.id.nav_action);
//                NavigationHelper navigationHelper = new NavigationHelper();
//                navigationHelper.setupDrawer(mToolbar);
                setupDrawer();
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

    @Override
    public void onClick(View view) {
//        if(view.getId()==R.id.btn_LogOut){
//            AuthUI.getInstance()
//                    .signOut(this)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            Log.d("AUTH", "USER LOGGED OUT");
//                            finish();
//                        }
//                    });
//        }
    }

    private void setupDrawer() {
        Toolbar mToolbar;
        NavigationView navigationView;

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

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        NavigationHelper nh = new NavigationHelper();
        nh.setHeaderDetails(auth,navigationView, getApplicationContext());

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
                            .setLogo(R.drawable.icon_svg)
                            .setProviders(
                                    AuthUI.FACEBOOK_PROVIDER)
                            .build(), RC_SIGN_IN);
                    Log.d("NAV", "SIGN OUT");
                }

                if(id == R.id.nav_close_app){
                    moveTaskToBack(true);
                    finish();
                }

                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        navigationView.getMenu().getItem(0).setChecked(true);
    }
//
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
                            //finish();
                        }
                    });
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.icon_svg)
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
    public void openChatActivity(View view){
        Intent intent = new Intent(this,ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
