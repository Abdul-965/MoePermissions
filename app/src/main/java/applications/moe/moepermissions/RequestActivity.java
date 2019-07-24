package applications.moe.moepermissions;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RequestActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    private Spinner hourSpinner, miniuteSpinner, returnHourSpinner, returnMinuteSpinner; // define spinner
    private int startHour = 7, startMiniute = 0, returnHour = 7, returnMinute = 0; // define time values
    private Button btn_add_request;
    private EditText cDate, reasonText;
    private Switch endOfDaySwitch;
    private Context context; // store app context
    private int mYear, mMonth, mDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        getSupportActionBar().hide();
        context = getApplicationContext();
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        hourSpinner = (Spinner) findViewById(R.id.hour);
        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                startHour = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        miniuteSpinner = (Spinner) findViewById(R.id.minute);
        miniuteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                startMiniute = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        returnHourSpinner = (Spinner) findViewById(R.id.hourReturn);
        returnHourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                returnHour = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        returnMinuteSpinner = (Spinner) findViewById(R.id.minuteReturn);
        returnMinuteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                returnMinute = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        cDate = (EditText) findViewById(R.id.cDate);
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date today = new Date();
        String todayString  = timeStampFormat.format(today);
        cDate.setText(todayString, TextView.BufferType.EDITABLE);
        cDate.setOnClickListener(this);
        btn_add_request = (Button) findViewById(R.id.btn_add_request);
        btn_add_request.setOnClickListener(this);

        // handle end Of Day Switch
        endOfDaySwitch = (Switch) findViewById(R.id.endOfDaySwitch);
        endOfDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) { // disable return time
                    returnHourSpinner.setSelection(7);
                    returnMinuteSpinner.setSelection(0);
                    returnHourSpinner.setEnabled(false);
                    returnMinuteSpinner.setEnabled(false);
                }
                else {
                    returnHourSpinner.setEnabled(true);
                    returnMinuteSpinner.setEnabled(true);
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        if(view == btn_add_request) {
            // calculate total start minutes
            int totalStartMinutes = startHour * 60 + startMiniute;
            // calculate total end minutes
            int totalEndMinutes = returnHour * 60 + returnMinute;
            if (totalEndMinutes - totalStartMinutes > 180) { // maximum of three hours
                Toast.makeText(getApplicationContext(), "Maximum permission time is three hours", Toast.LENGTH_LONG).show();
            }
            else {
                String _date = cDate.getText().toString();
                String _hour = hourSpinner.getSelectedItem().toString();
                String _minute = miniuteSpinner.getSelectedItem().toString();;
                reasonText = (EditText) findViewById(R.id.reason);
                String _reason = reasonText.getText().toString();
                int _restOfDay =  endOfDaySwitch.isActivated() == true ? 1 : 0;
                String _return_h = returnHourSpinner.getSelectedItem().toString();;
                String _return_m = returnMinuteSpinner.getSelectedItem().toString();;
                int _status = 0;
                String _uid = firebaseAuth.getCurrentUser().getUid();

                Map<String, String> permissionMap = new HashMap<>();
                permissionMap.put("_date", _date);
                permissionMap.put("_hour", _hour);
                permissionMap.put("_minute", _minute);
                permissionMap.put("_reason", _reason);
                permissionMap.put("_restOfDay", String.valueOf(_restOfDay));
                permissionMap.put("_return_h",_return_h);
                permissionMap.put("_return_m",_return_m);
                permissionMap.put("_status", String.valueOf(_status));
                permissionMap.put("_uid", _uid);

                Task<DocumentReference> Addpermission = firestore.collection("permissions").add(permissionMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(Task<DocumentReference> task) {
                        Toast.makeText(getApplicationContext(), "Request sent for approval", Toast.LENGTH_LONG).show();
                    }
                });

            }
        }
        if (view == cDate) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            cDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

    }
}
