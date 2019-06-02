package ntduong.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceGroup;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.transform.Result;

public class PostActivity extends AppCompatActivity {
    Toolbar mToolbar;
    ImageButton SelectPostImage;
    EditText PostDecription;
    Button UpdatePostButton;
    final static int Gallery_Pick = 1;
    ProgressDialog loadingBar;
    Uri ImageUri;
    String Description;
    StorageReference PostImagesReference;
    DatabaseReference UsersRef, PostRef;
    String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        PostImagesReference = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        SelectPostImage = findViewById(R.id.select_post_image);
        PostDecription = findViewById(R.id.post_description);
        UpdatePostButton = findViewById(R.id.update_post_button);
        loadingBar = new ProgressDialog(this);

        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        Description  =  PostDecription.getText().toString();
        if(ImageUri == null){
            Toast.makeText(this, "Please select post image..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Please say something about image..", Toast.LENGTH_SHORT).show();

        }
        else{
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();
        }

    }

    private void StoringImageToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate  = new SimpleDateFormat("dd-MMMM-yyyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime  = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = PostImagesReference.child("Post Image")
                .child(ImageUri.getLastPathSegment() + postRandomName + "jpg");
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                    Toast.makeText(PostActivity.this, "Image upload secessfully to Storage Firebase",
                            Toast.LENGTH_SHORT).show();
                    SavingPostInformationDatabase();
                }
                else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured: "+message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformationDatabase() {
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullName").getValue().toString();
                    String userProfileImage= dataSnapshot.child("profileimage").getValue().toString();
                    HashMap postMap = new HashMap();
                        postMap.put("uid",current_user_id);
                        postMap.put("date",saveCurrentDate);
                        postMap.put("time",saveCurrentTime);
                        postMap.put("description",Description);
                        postMap.put("postimage",downloadUrl);
                        postMap.put("profileimage",userProfileImage);
                        postMap.put("fullname",userFullName);
                    PostRef.child(current_user_id + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                SenUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New post is updated sucessfully",
                                        Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else {
                                Toast.makeText(PostActivity.this, "Error occured while updating your post",
                                        Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data!= null){
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id  = item.getItemId();
        if(id == android.R.id.home){
            SenUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SenUserToMainActivity() {
        startActivity(new Intent(PostActivity.this,MainActivity.class));
    }
}