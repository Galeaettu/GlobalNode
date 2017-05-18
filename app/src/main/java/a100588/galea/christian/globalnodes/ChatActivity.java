package a100588.galea.christian.globalnodes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 0;
    private static final int GALLERY_INTENT = 2;
    private static final int CAMERA_REQUEST_CODE = 1;
    public static final String PREFS_NAME = "TimeElapsed";
    public static final String PREFS_NAME_DIFF = "TimeElapsed_Diff";
    public static final String PREFS_NAME_TOTAL = "TimeElapsed_Total";
    public static final int MY_CAMERA_REQUEST_RESULT = 3;
    private FirebaseAuth auth;
    private StorageReference mStorage;

    private String mMessageKey = null;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private DatabaseReference mDatabase;
    private FirebaseListAdapter<ChatMessage> adapter;

    public long timeElapsed = 1000;

    private Uri fileUri;

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
            ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        NavigationHelper nh = new NavigationHelper();
        nh.setHeaderDetails(navigationView, getApplicationContext());

        Toolbar mToolbar = (Toolbar) findViewById(R.id.nav_action);
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

                if(id == R.id.nav_home){
                    openHome(getCurrentFocus());
                }

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

        navigationView.getMenu().getItem(2).setChecked(true);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        registerForContextMenu(fab);
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
                            .setValue(new ChatMessage(
                                    input.getText().toString()
                                    , FirebaseAuth.getInstance().getCurrentUser().getDisplayName())
                            );

                    // Clear the input
                    input.setText("");
                }else{
                    Toast.makeText(ChatActivity.this, "Enter some text.\nHold button to send an image.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME_DIFF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        timeElapsed = System.currentTimeMillis();
        long shared_elapsed_time_diff = sharedPref.getLong(getString(R.string.saved_default_time_diff), timeElapsed);
        editor.putLong(getString(R.string.saved_default_time_diff), timeElapsed);
        editor.apply();
        Log.d("CHAT-CREATE",Long.toString(shared_elapsed_time_diff));
    }

    @Override
    protected void onPause() {
        super.onPause();

        setSharedPreferences();
        Log.d("CHAT-PAUSE", Long.toString(timeElapsed));
    }

    @Override
    protected void onStop() {
        super.onStop();

        setSharedPreferences();
        Log.d("CHAT-STOP", Long.toString(timeElapsed));
    }

    private void setSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        timeElapsed = System.currentTimeMillis();
        SharedPreferences sharedPrefDiff = getSharedPreferences(PREFS_NAME_DIFF, Context.MODE_PRIVATE);
        long shared_elapsed_time_diff = sharedPrefDiff.getLong(getString(R.string.saved_default_time_diff), timeElapsed);

        timeElapsed = timeElapsed-shared_elapsed_time_diff;

        SharedPreferences sharedPrefTotal = getSharedPreferences(PREFS_NAME_TOTAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorTotal = sharedPrefTotal.edit();
        long shared_elapsed_time_total = sharedPref.getLong(getString(R.string.saved_default_time_total), timeElapsed);
        editorTotal.putLong(getString(R.string.saved_default_time_total), timeElapsed);
        editorTotal.apply();

        timeElapsed = timeElapsed+shared_elapsed_time_total;

        editor.putLong(getString(R.string.saved_default_time), timeElapsed);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView;
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(2).setChecked(true);

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME_DIFF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        timeElapsed = System.currentTimeMillis();
        long shared_elapsed_time_diff = sharedPref.getLong(getString(R.string.saved_default_time_diff), timeElapsed);
        editor.putLong(getString(R.string.saved_default_time_diff), timeElapsed);
        editor.apply();
        Log.d("CHAT-RESUME",Long.toString(shared_elapsed_time_diff));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final ProgressDialog mProgress = new ProgressDialog(ChatActivity.this);
        mProgress.setMessage("Loading Image...");

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
        if(requestCode == GALLERY_INTENT){
            if(resultCode == RESULT_OK){
                mProgress.setTitle(R.string.chat_send_image);
                mProgress.show();

                Uri uri = data.getData();

                StorageReference filepath = mStorage.child("Photos").child(uri.getLastPathSegment());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgress.dismiss();
                        Toast.makeText(ChatActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        Log.d("IMAGE LINK",taskSnapshot.getDownloadUrl().toString());
                        String imageLink = taskSnapshot.getDownloadUrl().toString();

                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Message")
                                .push()
                                .setValue(new ChatMessage(imageLink)
                                );
                    }
                });
            }
        }
        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                mProgress.setTitle(R.string.chat_send_image_camera);
                mProgress.show();

//                Bundle extras = data.getExtras();
//                Bitmap bitmap = (Bitmap) extras.get("data");
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] dataBAOS = baos.toByteArray();

                //fileUri = data.getData();

                //Log.d("CAMERA TEST",data.toString());
                mStorage = FirebaseStorage.getInstance().getReference();
                StorageReference filepath = mStorage.child("Photos").child(fileUri.getLastPathSegment()+ new Date().getTime());
                filepath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgress.dismiss();
                        Toast.makeText(ChatActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        Log.d("IMAGE LINK",taskSnapshot.getDownloadUrl().toString());
                        String imageLink = taskSnapshot.getDownloadUrl().toString();

                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Message")
                                .push()
                                .setValue(new ChatMessage(imageLink)
                                );
                    }
                });
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
    public void openHome(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                ImageView messageImage = (ImageView)v.findViewById(R.id.message_image);

                RelativeLayout messageContent = (RelativeLayout)v.findViewById(R.id.message_content);

                RelativeLayout.LayoutParams params_user_name = (RelativeLayout.LayoutParams)messageUser.getLayoutParams();
                RelativeLayout.LayoutParams params_user_content = (RelativeLayout.LayoutParams)messageContent.getLayoutParams();

                Drawable bg = messageContent.getBackground();
                GradientDrawable shapeDrawable = (GradientDrawable ) bg;

                if(auth.getCurrentUser().getUid().equals(model.getUserKey())){
                    params_user_name.removeRule(RelativeLayout.ALIGN_PARENT_START);
                    params_user_name.addRule(RelativeLayout.ALIGN_PARENT_END);
                    params_user_content.removeRule(RelativeLayout.ALIGN_PARENT_START);
                    params_user_content.addRule(RelativeLayout.ALIGN_PARENT_END);


                    shapeDrawable.setColor(ContextCompat.getColor(ChatActivity.this,R.color.chat_user_message));

                }else{
                    params_user_name.removeRule(RelativeLayout.ALIGN_PARENT_END);
                    params_user_name.addRule(RelativeLayout.ALIGN_PARENT_START);
                    params_user_content.removeRule(RelativeLayout.ALIGN_PARENT_END);
                    params_user_content.addRule(RelativeLayout.ALIGN_PARENT_START);

                    shapeDrawable.setColor(ContextCompat.getColor(ChatActivity.this,R.color.chat_inner_message));
                }


                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point(); display.getSize(size);
                int width = size.x;

                try {
                    if (model.getMessageImage().length() > 0) {
                        Log.d("MESSAGE PIC", model.getMessageImage());
                        messageImage.setVisibility(View.VISIBLE);
                        Picasso.with(ChatActivity.this).load(model.getMessageImage())
                                .resize(width, 0)
                                .onlyScaleDown()
                                .into(messageImage);
                        Log.d("MESSAGE PIC", "YEP");
                    }
                }catch (NullPointerException e){
                    messageImage.setVisibility(View.GONE);
                }

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
    public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int id = v.getId();

        switch (id){
            case R.id.list_of_messages:
                final MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.chat_context_menu, menu);


                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                mMessageKey = adapter.getRef(info.position).getKey();

                DatabaseReference mRefClicked = mDatabase.child("Message").child(mMessageKey);
                mRefClicked.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                        final String userKey = chatMessage.getUserKey();
                        Log.d("GOT USER KEY",userKey);

                        if (userKey.equals(auth.getCurrentUser().getUid())){
                            final String sentImage = chatMessage.getMessageImage();
                            if(!TextUtils.isEmpty(sentImage)){
                                menu.removeItem(R.id.chat_edit_item);
                                menu.add(Menu.NONE,R.id.chat_open_item,Menu.NONE, R.string.chat_open_image);
//                                menu.add(R.id.chat_open_item);
                            }

                        }else{
                            Toast.makeText(ChatActivity.this,"This is not your message",Toast.LENGTH_LONG).show();
                            menu.close();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ChatActivity.this,"No User Id available",Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.fab:
                MenuInflater inflater_imageSend = getMenuInflater();
                inflater_imageSend.inflate(R.menu.chat_image_context_menu, menu);
            break;
        }



    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.chat_delete_item:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mMessageKey = adapter.getRef(info.position).getKey();
                mDatabase.child("Message").child(mMessageKey).removeValue();
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ChatActivity.this,"Cancelled",Toast.LENGTH_LONG).show();
                    }
                });

                break;
            case R.id.chat_edit_item:
                final View view = (LayoutInflater.from(ChatActivity.this)).inflate(R.layout.chat_dialog_edit,null);
                final EditText editMessageText = (EditText)view.findViewById(R.id.chat_message_edit);
                final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                DatabaseReference mRef = mDatabase.child("Message").child(mMessageKey);
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                            final String messageTextToEdit = chatMessage.getMessageText();
                            Log.d("GOT TEXT", messageTextToEdit);
                            editMessageText.setText(messageTextToEdit);
                            builder.setTitle("Edit message");
                            builder.setView(view);
                            builder
                                    .setPositiveButton(R.string.chat_edit_text, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("EDIT TEXT", editMessageText.getText().toString());
                                            mDatabase.child("Message").child(mMessageKey).child("messageText")
                                                    .setValue(editMessageText.getText().toString());
                                        }
                                    });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }catch(Exception e){
                            Toast.makeText(ChatActivity.this, "Cannot change image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case R.id.chat_open_item:
                mRef = mDatabase.child("Message").child(mMessageKey);
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                            final String messageImage = chatMessage.getMessageImage();
                            openImageIntent(messageImage);
                        }catch(Exception e){
                            Toast.makeText(ChatActivity.this, "Cannot Open Image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Toast.makeText(ChatActivity.this, "Opening Image", Toast.LENGTH_SHORT).show();

                break;
            case R.id.chat_image_send:
                startImageIntent();
                break;
            case R.id.chat_image_camera_send:
                startCameraImageIntent();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void openImageIntent(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void startImageIntent(){
        mStorage = FirebaseStorage.getInstance().getReference();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_INTENT);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void startCameraImageIntent(){
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.CAMERA)) {
                    Log.d("CAMERA", "EXPLANATION");

                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.CAMERA},
                            MY_CAMERA_REQUEST_RESULT);

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    Log.d("CAMERA", "NO EXPLANATION");
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.CAMERA},
                            MY_CAMERA_REQUEST_RESULT);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                Log.d("CAMERA", "START CAM");
                startCamera();
            }
        }else{
            startCamera();
        }
    }

    private void startCamera() {
        mStorage = FirebaseStorage.getInstance().getReference();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                fileUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_CAMERA_REQUEST_RESULT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("CAMERA DENIED","ALLOWED");


                    startCamera();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(ChatActivity.this, "DENIED", Toast.LENGTH_LONG).show();
                    Log.d("CAMERA DENIED","DENIED");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
