package applications.moe.moepermissions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import applications.moe.moepermissions.applications.moe.moepermissions.dmgr.MoeUser;

import static android.content.ContentValues.TAG;

public class ManagerActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ListView simpleList;
    ArrayList<String> listEntry = new ArrayList<String>();
    private Context mContext;
    ListView simpleListView;
    CustomAdapter customAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        getSupportActionBar().hide();
        simpleList = (ListView) findViewById(R.id.simpleListView);
        mContext = getApplicationContext();
        customAdapter = new CustomAdapter(simpleList.getContext(), listEntry);
        simpleList.setAdapter(customAdapter);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        String entry = "Date   Start   End    Reason";
        listEntry.add(entry);
        getRequests();

    }

    public void getRequests() {
        CollectionReference permissions = firestore.collection("permissions");
        //Query query = users.get()
        //Query query = users.whereEqualTo("_uid", user.getUid());
        int _status = 0;
        final Task<QuerySnapshot> results = permissions.whereEqualTo("_status",String.valueOf(_status) ).get().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Fail " + e.getMessage());
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot result = task.getResult();
                for (DocumentSnapshot doc : result.getDocuments()) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    try {
                        Date requestDate = simpleDateFormat.parse(doc.get("_date").toString());
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(requestDate); // don't forget this if date is arbitrary e.g. 01-01-2014
                        String entry = String.valueOf(cal.DAY_OF_MONTH) + "-" + String.valueOf(cal.MONTH) + "  " + String.valueOf(doc.get("_hour")) + ":" +
                                String.valueOf(doc.get("_minute")) + "  " +  String.valueOf(doc.get("_return_h")) + ":" +
                                String.valueOf(doc.get("_return_m")) + " " + String.valueOf(doc.get("_reason"));

                        listEntry.add(entry);
                     } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
                customAdapter.notifyDataSetChanged();
            }
        });
    }
    public class CustomAdapter extends BaseAdapter {
        Context context;
        ArrayList entries;
        LayoutInflater inflter;

        public CustomAdapter(Context applicationContext, ArrayList entries) {
            this.context = applicationContext;
            this.entries = entries;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return entries.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }


        @SuppressLint({"ViewHolder", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflter.inflate(R.layout.activity_listview, null, false);
            TextView dList = (TextView) view.findViewById(R.id.dList);
            dList.setText(String.valueOf(entries.get(i)));
            return view;
        }
    }
}
