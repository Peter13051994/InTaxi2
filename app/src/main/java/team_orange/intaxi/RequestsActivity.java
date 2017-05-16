package team_orange.intaxi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class RequestsActivity extends AppCompatActivity {
public ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        mListView = (ListView) findViewById(R.id.mListView);
// 1
        final ArrayList<Request> reqList =  (ArrayList<Request>) getIntent().getSerializableExtra("reqs");
// 2

// 4
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, reqList);
        mListView.setAdapter(adapter);
    }
}
