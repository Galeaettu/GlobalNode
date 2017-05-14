package a100588.galea.christian.globalnodes;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

/**
 * Created by Chris on 10/05/2017.
 */
public class NavigationHelper extends AppCompatActivity{
    private static final int RC_SIGN_IN = 0;

    private TextView headerEmail;
    private TextView headerName;
    private ImageView headerImage;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;


    public void getHeaderDetails(NavigationView navigationView){
        View hView = navigationView.getHeaderView(0);
        headerEmail = (TextView)hView.findViewById(R.id.header_email);
        headerName = (TextView)hView.findViewById(R.id.header_name);
        headerImage = (ImageView)hView.findViewById(R.id.header_profile_image);
    }

    public void setHeaderDetails(FirebaseAuth auth, NavigationView navigationView, Context context){
        getHeaderDetails(navigationView);
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            headerName.setText(auth.getCurrentUser().getDisplayName());
            headerEmail.setText(auth.getCurrentUser().getEmail());

            Picasso.with(context).load(auth.getCurrentUser()
                    .getPhotoUrl())
                    .transform(new RoundedTransformation())
                    .into(headerImage);
        }
    }

    public void setupDrawer(Toolbar mToolbar) {
        NavigationView navigationView;

        final FirebaseAuth auth = FirebaseAuth.getInstance();


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
    }

    public void openUserDetails(View view){
        Intent intent = new Intent(this,AccountDetails.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
