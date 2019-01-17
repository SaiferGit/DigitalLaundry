package team.orion.androidcustomerapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class UserRequestActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request);

        Button btnPickupTime;
        Button btnDeliveryTime;
        Button btnSchedulePickup;

        btnPickupTime = (Button) findViewById(R.id.btn_pickup_time);
        btnDeliveryTime = (Button) findViewById(R.id.btn_delivery_time);
        btnSchedulePickup = (Button) findViewById(R.id.btn_schedule_pickup);

        btnPickupTime.setOnClickListener(this);
        btnDeliveryTime.setOnClickListener(this);
        btnSchedulePickup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_pickup_time:
                set(R.id.btn_pickup_time);
                break;
            case R.id.btn_delivery_time:
                set(R.id.btn_delivery_time);
                break;
            case R.id.btn_schedule_pickup:
                //Intent intent = new Intent(UserRequestActivity.this, LoginActivity.class);
                //startActivity(intent);
                break;

        }
    }

    //function to set the time
    public void setTime() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(UserRequestActivity.this, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                if (selectedHour > 11) {
                    if (selectedHour != 12) {
                        selectedHour %= 12;
                    }
                    if (selectedHour < 10 && selectedMinute < 10) {
                        btn.setText(btn.getText().toString() + "   0" + selectedHour + ":0" + selectedMinute + " PM");
                    } else if (selectedHour < 10) {
                        btn.setText(btn.getText().toString() + "   0" + selectedHour + ":" + selectedMinute + " PM");
                    } else if (selectedMinute < 10) {
                        btn.setText(btn.getText().toString() + "   " + selectedHour + ":0" + selectedMinute + " PM");
                    } else {
                        btn.setText(btn.getText().toString() + "   " + selectedHour + ":" + selectedMinute + " PM");
                    }
                } else {
                    if (selectedHour < 10 && selectedMinute < 10) {
                        btn.setText(btn.getText().toString() + "   0" + selectedHour + ":0" + selectedMinute + " AM");
                    } else if (selectedHour < 10) {
                        btn.setText(btn.getText().toString() + "   0" + selectedHour + ":" + selectedMinute + " AM");
                    } else if (selectedMinute < 10) {
                        btn.setText(btn.getText().toString() + "   " + selectedHour + ":0" + selectedMinute + " AM");
                    } else {
                        btn.setText(btn.getText().toString() + "   " + selectedHour + ":" + selectedMinute + " AM");
                    }
                }
                btn.setTextColor(getResources().getColor(R.color.shada));
                //btn.setBackground(getDrawable(R.drawable.button_background_selected)); replaced because required min API 21
                //btn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_selected)); replaced because required min API 16
                btn.setBackgroundResource(R.drawable.button_background_selected);
            }
        }, hour, minute, false);//Not 24 hour time
        //mTimePicker.setTitle("Select pickup time");
        mTimePicker.show();
    }

    //function to set date
    public void set(int x) {
        btn = findViewById(x);
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH);// current day
        DatePickerDialog datePickerDialog;
        datePickerDialog = new DatePickerDialog(UserRequestActivity.this, R.style.DialogTheme,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        btn.setText(dayOfMonth + " "
                                + theMonth(monthOfYear) + " " + year);
                    }
                }, mYear, mMonth, mDay);
        setTime();
        datePickerDialog.show();
    }

    public static String theMonth(int month) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month];
    }
}
