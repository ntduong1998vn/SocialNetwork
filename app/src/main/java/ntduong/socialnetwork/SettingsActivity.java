package ntduong.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    Toolbar mToolbar;
    EditText userName, userProfileName, userStatus, userCountry, userDateOfBirth, userGender, userRelation;
    Button updateAccountSettingButton;
    CircleImageView userProfileImage;

    StorageReference UserProfileImageRef;
    DatabaseReference userRef;
    FirebaseAuth mAuth;
    String currentUserID;
    final static int Gallery_Pick = 1;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Lấy UserID và thiết lập đường dẫn đến Database và kho lữu trữ dữ liệu
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        // Thiết lập toolbar
        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //
        userName = findViewById(R.id.settings_username);
        userProfileName = findViewById(R.id.settings_profile_full_name);
        userStatus = findViewById(R.id.settings_status);
        userCountry = findViewById(R.id.settings_country);
        userDateOfBirth = findViewById(R.id.settings_dob);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relatonship_status);
        updateAccountSettingButton = findViewById(R.id.update_account_settings_buttons);
        userProfileImage = findViewById(R.id.settings_profile_image);

        loadingBar = new ProgressDialog(this);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("userName").getValue().toString();
                    String fullName = dataSnapshot.child("fullName").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String country = dataSnapshot.child("country").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String relationshipstatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userName.setText(username);
                    userProfileName.setText(fullName);
                    userStatus.setText(status);
                    userCountry.setText(country);
                    userDateOfBirth.setText(dob);
                    userGender.setText(gender);
                    userRelation.setText(relationshipstatus);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateAccountSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateAccountInfo();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                // Get a reference to store file at chat_photos/<FILENAME>
                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Cập nhật hình ảnh thành công !", Toast.LENGTH_SHORT).show();
                            //When the image has successfully uploaded, get its download URL
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    userRef.child("profileimage").setValue(downloadUri)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                        startActivity(selfIntent);

                                                        Toast.makeText(SettingsActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
//                                                        Toast.makeText(SetupActivity.this, "Lưu trữ hình ảnh thành công !", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SettingsActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });

            } else {
                Toast.makeText(SettingsActivity.this, "Lỗi! Image can't be cropped. Try Again!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String fullName = userProfileName.getText().toString();
        String status = userStatus.getText().toString();
        String country = userCountry.getText().toString();
        String dob = userDateOfBirth.getText().toString();
        String gender = userGender.getText().toString();
        String relationshipstatus = userRelation.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Vui lòng nhập UserName!!!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Vui lòng nhập họ tên đầy đủ!!!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Vui lòng nhập status!!!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Vui lòng nhập quốc tịch!!!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Vui lòng nhập ngày sinh!!!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Vui lòng chọn giới tính!!!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(relationshipstatus)) {
            Toast.makeText(this, "Vui lòng nhập tình trạng hôn nhân hiện tại!!!", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we updating your profile image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username, fullName, status, country, dob, gender, relationshipstatus);
        }

    }

    private void UpdateAccountInfo(String username, String fullName, String status, String country, String dob, String gender, String relationshipstatus) {
        HashMap userMap = new HashMap();
        userMap.put("userName", username);
        userMap.put("fullName", fullName);
        userMap.put("status", status);
        userMap.put("country", country);
        userMap.put("dob", dob);
        userMap.put("gender", gender);
        userMap.put("relationshipstatus", relationshipstatus);

        // Cập nhật thông tin
        userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Cập nhật thành công !!!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(SettingsActivity.this, "Xảy ra lỗi !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //
    void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
