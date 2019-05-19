package com.example.textme;

import android.content.Intent;
import android.os.Bundle;

import com.example.textme.Adapters.UsersAdapter;
import com.example.textme.Models.Friend;
import com.example.textme.Models.User;
import com.example.textme.ViewHolders.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class addFriendsActivity extends AppCompatActivity {

    RecyclerView rvUsers;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
    List<User> users = new ArrayList<>();
    List<String> friendIds = new ArrayList<>();
    List<User> filterdUsers = new ArrayList<>();
    SearchView searchBar;
    UsersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        setSupportActionBar(toolbar);
        FindViews(); // INITING VIEWS
        initSearch();
        FetchFriendsIds(); // GETTING LIST OF FRIENDS IDS
        rvUsers.setAdapter(new UsersAdapter()); // SETTING EMEPTY ADAPTER FOR INSTANT RECYCLER INIT
        FetchUsers(friendIds); // Fetching USERS
    }

    @Override
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
                rvUsers.postDelayed(()->{
                    Intent i = new Intent(this,MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                },1000);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void FindViews(){
        rvUsers = findViewById(R.id.rvUsers);
        searchBar = findViewById(R.id.addFriendsSearch);

    }

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

    private void filterUsers(String newText){
        if(filterdUsers.size() != 0){
            filterdUsers.clear();
        }
        for(User user : users){
            if(user.getDisplayName().contains(newText)){
                filterdUsers.add(user);
            }
        }
        adapter = new UsersAdapter(filterdUsers,getApplicationContext(),friendIds);
        rvUsers.setAdapter(adapter);
    }

    //Getting ids of Friends
    private void FetchFriendsIds(){
        Query query = FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("uid");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Friend friend = snapshot.getValue(Friend.class);
                    friendIds.add(friend.uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Getting Users
    private void FetchUsers(List<String> friendIds){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    User user = snapshot.getValue(User.class);
                    if(!(user.getId().matches(FirebaseAuth.getInstance().getCurrentUser().getUid())))
                    {
                        users.add(user);
                    }
                }
                System.out.println(users);
                // Fetching Recycler
                adapter = new UsersAdapter(users,getApplicationContext(),friendIds);
                rvUsers.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rvUsers.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.HORIZONTAL));
                rvUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

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


}
