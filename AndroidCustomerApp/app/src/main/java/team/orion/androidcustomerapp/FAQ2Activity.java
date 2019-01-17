package team.orion.androidcustomerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class FAQ2Activity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq2);

        //adding toolbar in main activity
        mToolbar = (Toolbar) findViewById(R.id.faq_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Digital Laundry FAQs");
    }
}
