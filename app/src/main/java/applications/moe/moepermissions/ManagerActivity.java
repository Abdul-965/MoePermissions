package applications.moe.moepermissions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ManagerActivity extends AppCompatActivity {

    ListView simpleList;
    String names[] = {"User1", "User2", "User3"};
    String dates[] = {"10/5/2019", "10/6/2019", "13/5/2019"};
    private Context mContext;
    ListView simpleListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        getSupportActionBar().hide();
        simpleList = (ListView) findViewById(R.id.simpleListView);
        mContext = getApplicationContext();

        CustomAdapter customAdapter = new CustomAdapter(simpleList.getContext(), names, dates);
        simpleList.setAdapter(customAdapter);

    }

    public class CustomAdapter extends BaseAdapter {
        Context context;
        String names[];
        String dates[];
        LayoutInflater inflter;

        public CustomAdapter(Context applicationContext, String[] names, String[] dates) {
            this.context = applicationContext;
            this.names = names;
            this.dates = dates;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return names.length;
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
            TextView name = (TextView) view.findViewById(R.id.name);
            TextView date = (TextView) view.findViewById(R.id.date);
            name.setText(names[i]);
            date.setText(dates[i]);
            return view;
        }
    }
}
