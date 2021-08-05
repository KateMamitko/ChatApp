package com.example.chatapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    private static final int RC_GET = 101;
    private RecyclerView recyclerViewMassege;
    private MassegeAdapter adapter;

    private EditText editTextMassege;
    private ImageView imageViewSendMassege;
    private ImageView imageViewAdd;

    private SharedPreferences preferences;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseAuthUIAuthenticationResult result;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.itemSignOut) {
            mAuth.signOut();
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        editTextMassege = findViewById(R.id.editTextMassege);
        imageViewSendMassege = findViewById(R.id.imageViewSendMassege);
        recyclerViewMassege = findViewById(R.id.recyclerViewMassege);
        recyclerViewMassege.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MassegeAdapter(this);
        recyclerViewMassege.setAdapter(adapter);
        imageViewAdd = findViewById(R.id.imageViewAddImage);
        db = FirebaseFirestore.getInstance();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        imageViewSendMassege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmassege(editTextMassege.getText().toString().trim(),null);
            }
        });
        imageViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent, RC_GET);
            }
        });

        if (mAuth.getCurrentUser() != null) {
            preferences.edit().putString("author",mAuth.getCurrentUser().getEmail()).apply();
        } else {
            signOut();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.collection("messages")
                .orderBy("data")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            List<Massege> masseges = value.toObjects(Massege.class);
                            adapter.setMasseges(masseges);
                            recyclerViewMassege.scrollToPosition(adapter.getItemCount()-1);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    final StorageReference reference = storageRef.child("image/" + uri.getLastPathSegment());
                    reference.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return reference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                if (downloadUri != null){
                                    sendmassege(null,downloadUri.toString());
                                }
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }
                if (requestCode == RC_SIGN_IN) {
                    IdpResponse response = IdpResponse.fromResultIntent(data);

                    if (result.getResultCode() == RESULT_OK) {
                        // Successfully signed in
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(MainActivity.this, user.getEmail(), Toast.LENGTH_SHORT).show();
                            preferences.edit().putString("author",user.getEmail()).apply();
                        }
                    } else {
                        if (response != null) {
                            Toast.makeText(MainActivity.this, "Error" + response.getError(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    private void sendmassege (String text, String urlToImage) {
        Massege massege = null;
        String author = preferences.getString("author", "Anonim");
        if (!text.isEmpty()) {
            massege = new Massege(author, text, System.currentTimeMillis(),null);
        } else if (urlToImage != null && !urlToImage.isEmpty()){
            massege = new Massege(author, null, System.currentTimeMillis(),urlToImage);
        }
        db.collection("messages")
                .add(massege)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        editTextMassege.setText("");
                        recyclerViewMassege.scrollToPosition(adapter.getItemCount() - 1);
                    }
                });

    }

    public void signOut() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
}