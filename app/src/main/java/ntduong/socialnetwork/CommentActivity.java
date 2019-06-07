package ntduong.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {
    DatabaseReference UserRef,PostRef;
    FirebaseAuth mAuth;
    RecyclerView CommentList;
    ImageButton PostCommentButton;
    EditText CommentInputText;
    String Post_Key, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        CommentList = findViewById(R.id.comments_list);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(linearLayoutManager);

        CommentInputText = findViewById(R.id.comment_input);
        PostCommentButton = findViewById(R.id.post_comment_btn);
        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName = dataSnapshot.child("userName").getValue().toString();
                            ValidateComment(userName);
                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(PostRef, Comments.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments,CommentsViewHolder>(options){
            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);

                return new CommentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int i, @NonNull Comments comments) {
                commentsViewHolder.setUsername(comments.getUsername());
                commentsViewHolder.setComment(comments.getComment());
                commentsViewHolder.setDate(comments.getDate());
                commentsViewHolder.setTime(comments.getTime());
            }





        };
        firebaseRecyclerAdapter.startListening();
        CommentList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setUsername(String username){
            TextView myUserName = mView.findViewById(R.id.comment_username);
            myUserName.setText("@" + username + " ");
        }
        public void setComment(String comment){
            TextView myComment = mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }
        public void setDate(String date){
            TextView myDate = mView.findViewById(R.id.comment_date);
            myDate.setText(" Date: "+ date);
        }
        public void setTime(String time){
            TextView myTime = mView.findViewById(R.id.comment_time);
            myTime.setText(" Time: " + time);
        }
    }
    private void ValidateComment(String userName) {
        String commentText = CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please write text your comment...", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordTime.getTime());

            final String RandomKey =   current_user_id  + saveCurrentDate + saveCurrentTime;
            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);
            PostRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommentActivity.this, "You have comment secessfully!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(CommentActivity.this, "Error, try again!", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }
}
