package a100588.galea.christian.globalnodes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 0;
    private FirebaseAuth auth;

    private String mMessageKey = null;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private NavigationView navigationView;
    private Toolbar mToolbar;

    private DatabaseReference mDatabase;
    private ListView listOfMessages;
    private FirebaseListAdapter<ChatMessage> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        FacebookSdk.sdkInitialize(getApplicationContext());

        mDrawerLayout = (DrawerLayout)findViewById(R.id.chat_activity_drawer_layout);
        mActivityTitle = getTitle().toString();

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            //user already signed in
            Log.d("AUTH ACTIVITY", auth.getCurrentUser().getEmail());
            displayChatMessages();
            listOfMessages = (ListView)findViewById(R.id.list_of_messages);
            listOfMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
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

        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        navigationView.getMenu().getItem(1).setChecked(true);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputEditText input = (TextInputEditText)findViewById(R.id.input);
                if (input.length() > 0){
                    // Read the input field and push a new instance
                    // of ChatMessage to the Firebase database
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Message")
                            .push()
                            .setValue(new ChatMessage(input.getText().toString(),
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getDisplayName())
                            );

                    // Clear the input
                    input.setText("");
                }
            }
        });
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

                displayChatMessages();
            }
            else{
                //User not authenticated
                Log.d("AUTH", "NOT AUTHENTICATED");
                finish();
            }
        }
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

    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        final ProgressDialog mProgress = new ProgressDialog(ChatActivity.this);
        mProgress.setMessage("Loading Messages...");
        mProgress.setTitle(R.string.chat);
        mProgress.show();

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference().child("Message")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {

                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);


                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));

                mProgress.dismiss();
            }
        };

        listOfMessages.setAdapter(adapter);

        registerForContextMenu(listOfMessages);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_context_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        mMessageKey = adapter.getRef(info.position).getKey();
        switch (id){
            case R.id.chat_delete_item:
                Log.d("MENU DELETE", mMessageKey);
                mDatabase.child("Message").child(mMessageKey).removeValue();
                Log.d("MENU DELETE", info.toString());
                Log.d("MENU DELETE", mMessageKey);
                Log.d("MENU DELETE", Integer.toString( info.position));
                break;
            case R.id.chat_edit_item:
                break;
        }
        return super.onContextItemSelected(item);
    }
}
