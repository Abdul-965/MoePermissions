package applications.moe.moepermissions;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import applications.moe.moepermissions.applications.moe.moepermissions.dmgr.MoeUser;
import applications.moe.moepermissions.applications.moe.moepermissions.dmgr.MoeUserAPI;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {


    // get UI
    AutoCompleteTextView mEmailView;
    EditText mPasswordView;

    // define firebase related
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseUser user;
    MoeUser moeUser;
    View focusView;
    Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        appContext = getApplicationContext();
        // set firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        user = null;
        moeUser = new MoeUser(); // init Moe user
        Button mEmailSignInButton = (Button) findViewById(R.id.btn_signIn);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() { // call when click sign
            @Override
            public void onClick(View view) {
                FirebaseApp.initializeApp(getApplicationContext());
                FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);

                // get login details
                mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
                mPasswordView = (EditText) findViewById(R.id.password);


                mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                        if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                            try {
                                attemptLogin();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                        return false;
                    }
                });

                Button mEmailSignInButton = (Button) findViewById(R.id.btn_signIn);
                mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            attemptLogin(); // check login - validate inputs and call Firebase
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    private void attemptLogin() throws ExecutionException, InterruptedException {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = false;
        focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // call
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success " + firebaseAuth.getCurrentUser().getEmail());
                        user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String _uid = user.getUid();
                            CollectionReference users = firestore.collection("users");
                            //Query query = users.get()
                            //Query query = users.whereEqualTo("_uid", user.getUid());
                            final Task<QuerySnapshot> results = users.whereEqualTo("_uid", user.getUid()).get().addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Fail " + e.getMessage());
                                    moeUser = null;
                                }
                            }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    QuerySnapshot result = task.getResult();
                                    for (DocumentSnapshot doc : result.getDocuments()) {
                                        moeUser = new MoeUser(doc.get("_uid").toString(), doc.get("_name").toString(), Integer.parseInt(doc.get("_role").toString()));
                                        Log.d(TAG, "Name: " + doc.get("_name").toString());
                                        if (moeUser != null) {
                                            String role = moeUser.get_role() == 1 ? "Manager" : "Employee";
                                            Toast.makeText(appContext, "Welcome " + moeUser.get_name() + ", " + role, Toast.LENGTH_LONG).show();
                                            Boolean _isManager = moeUser.get_role() == 1;
                                            if(_isManager) { //open manager activity
                                                Intent myIntent = new Intent(LoginActivity.this, ManagerActivity.class);
                                                startActivity(myIntent);
                                            }
                                            else { // open employee activity
                                                Intent myIntent = new Intent(LoginActivity.this, RequestActivity.class);
                                                startActivity(myIntent);
                                            }
                                        } else {
                                            Toast.makeText(appContext, "Invalid login", Toast.LENGTH_LONG).show();
                                            focusView = mEmailView;
                                            focusView.requestFocus();
                                        }

                                    }
                                }
                            });

                        }
                    } else
                        Log.d(TAG, task.getException().getMessage());
                }
            });
        }
    }

    // validate email
    private boolean isEmailValid(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isPasswordValid(String password) {

        return password.length() >= 6;
    }
}

