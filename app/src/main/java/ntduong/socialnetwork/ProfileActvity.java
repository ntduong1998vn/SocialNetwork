package ntduong.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActvity extends AppCompatActivity {

    TextView userName, userProfileName, userStatus, userCountry, userDateOfBirth, userGender, userRelation;
    CircleImageView userProfileImage;

    DatabaseReference profileUserRef;
    FirebaseAuth mAuth;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_actvity);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);


        userName = findViewById(R.id.my_profile_username);
        userProfileName = findViewById(R.id.my_profile_full_name);
        userStatus = findViewById(R.id.my_profile_status);
        userCountry = findViewById(R.id.my_profile_country);
        userDateOfBirth = findViewById(R.id.my_profile_dob);
        userGender = findViewById(R.id.my_profile_gender);
        userRelation = findViewById(R.id.my_profile_relationship_status);
        userProfileImage = findViewById(R.id.my_profile_image);

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String username = dataSnapshot.child("userName").getValue().toString();
                    String fullName = dataSnapshot.child("fullName").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String country = dataSnapshot.child("country").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String relationshipstatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userName.setText("@"+username);
                    userProfileName.setText(fullName);
                    userStatus.setText(status);
                    userCountry.setText("Quốc tịch: "+country);
                    userDateOfBirth.setText("Ngày sinh: "+dob);
                    userGender.setText("Giới tính: "+gender);
                    userRelation.setText("Tình trạng hôn nhân: "+relationshipstatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
}
