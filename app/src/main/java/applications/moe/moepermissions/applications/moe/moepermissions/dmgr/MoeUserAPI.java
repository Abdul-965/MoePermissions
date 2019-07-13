package applications.moe.moepermissions.applications.moe.moepermissions.dmgr;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.core.ApiFuture;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.ContentValues.TAG;

public class MoeUserAPI {

    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    private AppCompatActivity activity;
    private FirebaseUser user;
    private MoeUser moeUser;
    // attach target activity: login activity
    public MoeUserAPI(AppCompatActivity target){
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        activity = target;
        user = null;
        moeUser = new MoeUser();
    }
    // login to system
    public void login(String email, String password) throws ExecutionException, InterruptedException {

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "signInWithEmail:success " + firebaseAuth.getCurrentUser().getEmail() );
                    user = firebaseAuth.getCurrentUser();
                    if (user!=null){
                        String _uid = user.getUid();
                        CollectionReference users = firestore.collection("users");
                        //Query query = users.get()
                        //Query query = users.whereEqualTo("_uid", user.getUid());
                        final Task<QuerySnapshot> results= users.whereEqualTo("_uid" , user.getUid()).get().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Fail " + e.getMessage());
                                moeUser = null;
                            }
                        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                QuerySnapshot result = task.getResult();
                                for (DocumentSnapshot doc: result.getDocuments()) {
                                    moeUser=new MoeUser(doc.get("_uid").toString(),doc.get("_name").toString(), Integer.parseInt(doc.get("_role").toString()));
                                    Log.d(TAG, "Name: " + doc.get("_name").toString());

                                }
                            }
                        });

                    }
                }
                else
                    Log.d(TAG, task.getException().getMessage());
            }
        });

    }
    // create new user
    public boolean createUser(String email, String password){
        String msg = "";
        final Task<AuthResult> authResultTask = firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "addUser:success");
                } else
                    Log.d(TAG, "Message " + task.getException().getMessage());
                user = null;
            }
        });
        user = firebaseAuth.getCurrentUser();
        return user!=null;
    }
    // logout
    public void logout() {
        moeUser = null;
        firebaseAuth.signOut();
    }

    // return current user
    public MoeUser getUser() {
        return moeUser;
    }
}
