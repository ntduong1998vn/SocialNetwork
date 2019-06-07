package ntduong.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.nio.file.Files;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ImageButton searchButton;
    EditText searchInput;
    RecyclerView searchResultList;

    DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        mToolbar = findViewById(R.id.find_friends_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tìm kiếm bạn bè");

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchButton = findViewById(R.id.search_people_friends_button);
        searchInput = findViewById(R.id.search_box_input);

        searchResultList = findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchBoxInput = searchInput.getText().toString();

                SearchPeopleAndFriends(searchBoxInput);
            }
        });
    }

    private void SearchPeopleAndFriends(String searchBoxInput) {

        Toast.makeText(this,"Searching....",Toast.LENGTH_SHORT).show();

        Query searchPeopleQuery = allUsersDatabaseRef.orderByChild("fullName")
                .startAt(searchBoxInput).endAt(searchBoxInput+"\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchPeopleQuery, FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);

                return new FindFriendsViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, int i, @NonNull FindFriends findFriends) {
                findFriendsViewHolder.setFullName(findFriends.getFullName());
                findFriendsViewHolder.setStatus(findFriends.getStatus());
                findFriendsViewHolder.setProfileImage(findFriends.getProfileimage());
            }


        };

        firebaseRecyclerAdapter.startListening();
        searchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
       View mView;

       public FindFriendsViewHolder(View itemView){
           super(itemView);

           this.mView = itemView;
       }

       public void setProfileImage(String profileImage){
           CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
           Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(myImage);
       }

       public void setFullName(String fullname){
           TextView myName = mView.findViewById(R.id.all_users_profile_full_name);
           myName.setText(fullname);
       }

       public void setStatus(String status){
           TextView mystatus = mView.findViewById(R.id.all_users_status);
           mystatus.setText(status);
       }



    }
}
