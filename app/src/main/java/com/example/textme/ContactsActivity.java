package com.example.textme;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.textme.Adapters.ContactsAdapter;
import com.example.textme.Models.Friend;
import com.example.textme.Models.User;
import com.example.textme.ViewHolders.ContactsViewHolder;
import com.firebase.ui.database.FirebaseArray;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    // *********** VARIABLES **************

    // Views
    SearchView searchBar;
    RecyclerView rvContacts;
    TextView tvAddFriends;
    TextView tvTitle;
    ImageView circleProfileImage;
    // REFs
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
    // Adapter
    private ContactsAdapter adapter;
    // Arrays
    private List<User> users = new ArrayList<>();
    private List<User> filterdUsers = new ArrayList<>();
    private ArrayList<String> friendsIds = new ArrayList<>();
    // Animations
    Animation fadeIn;


    // On Create Method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        findViews(); //---- Init Views -----

        //TODO: FIX THIS TO SHOW ONLY ON COLLAPES
        //tvTitle.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        fetchProfileImage(circleProfileImage);
        initSearch();
        setRvContacts(rvContacts); // Setting Recycler View

        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(i);
            }
        });

        tvAddFriends.setOnClickListener((v)->{
            Intent intent = new Intent(getApplicationContext(),addFriendsActivity.class);
            startActivity(intent);
        });

    }

    // Finding Views Method
    private void findViews(){
        rvContacts = findViewById(R.id.rvContacts);
        searchBar = findViewById(R.id.searchBar);
        tvAddFriends = findViewById(R.id.addFriendsBtn);
        fadeIn = new AlphaAnimation(0.0f,1.0f);
        fadeIn.setDuration(1500);
        circleProfileImage = findViewById(R.id.ciContactsProfileImage);
        tvTitle = findViewById(R.id.tvContactsNavbarTitle);
    }

    // Fetching Profile Image

    private void fetchProfileImage(ImageView iv){
        FirebaseStorage.getInstance().getReference().child("users_profile_images").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).fitCenter().centerCrop().into(iv);
            }
        });
    }

    // SearchBar Init
    private void initSearch(){
        searchBar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        searchBar.setQueryHint("Search...");
        searchBar.setVisibility(View.GONE);
        searchBar.setLabelFor(R.id.action_search);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return false;
            }
        });
    }

    // Filtering Users From SearchBar Query
    private void filterUsers(String newText) {
                filterdUsers.clear();
                for(User user : users){
                    if(user.getDisplayName().contains(newText)){
                        filterdUsers.add(user);
                    }
                }
                ContactsAdapter adapter = new ContactsAdapter(filterdUsers,getApplicationContext(),getSupportFragmentManager());
                rvContacts.setAdapter(adapter);
    }


    // Setting the Adapter and Reseting Array if not Null
    private void setRvContacts(RecyclerView rv){
        if(friendsIds.size() != 0) {
            friendsIds.clear();
        }

        if(users.size() != 0) {
            users.clear();
        }
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        getFriendsIds();
        rvContacts.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
    }


        // Collapsing and Expanding Animations
        public static void expand(final View v){
            v.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            final int targetHeight = v.getMeasuredHeight();

            // Older versions of android (pre API 21) cancel animations for views with a height of 0.
            v.getLayoutParams().height = 1;
            v.setVisibility(View.VISIBLE);
            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    v.getLayoutParams().height = interpolatedTime == 1
                            ? WindowManager.LayoutParams.WRAP_CONTENT
                            : (int)(targetHeight * interpolatedTime);
                    v.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            // 1dp/ms
            a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
            v.startAnimation(a);
        }

        public static void collapse(final View v){
            final int initialHeight = v.getMeasuredHeight();

            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if(interpolatedTime == 1){
                        v.setVisibility(View.GONE);
                    }else{
                        v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                        v.requestLayout();
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            // 1dp/ms
            a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
            v.startAnimation(a);
        }


    // ************************************
    // ---------- MENU METHODS ------------
    // ************************************

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contacts, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search: if(searchBar.getVisibility() == View.VISIBLE){
                collapse(searchBar);
            } else expand(searchBar);
            break;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                rvContacts.postDelayed(()->{
                    Intent i = new Intent(this,MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                },1000);
            break;
            case  R.id.action_profile:
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                i.putExtra("id",FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ****************************************
    // ---------- DATABASE METHODS ------------
    // ****************************************

    // Getting Contacts Array
    private void getFriendsIds(){
        Query query = FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("uid");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Friend friend = snapshot.getValue(Friend.class);
                    friendsIds.add(friend.uid);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                User user = snapshot.getValue(User.class);

                                if(!(user.getId().matches(FirebaseAuth.getInstance().getCurrentUser().getUid())) ){
                                    if(friend.uid.matches(user.getId())) {
                                        users.add(user);
                                    }
                                }
                            }

                            adapter = new ContactsAdapter(users,getApplicationContext(),getSupportFragmentManager());
                            rvContacts.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Reseting and ReBuilding UI on Stop and Restart Conditions

    @Override
    protected void onStop() {
        super.onStop();
        if(users.size() != 0){
            users.clear();
        }

        if(friendsIds.size() != 0){
            friendsIds.clear();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(users.size() != 0){
            users.clear();
        }

        if(friendsIds.size() != 0){
            friendsIds.clear();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(users.size() != 0){
            users.clear();
        }

        if(friendsIds.size() != 0){
            friendsIds.clear();
        }
        getFriendsIds();
        adapter.notifyDataSetChanged();
    }
}
