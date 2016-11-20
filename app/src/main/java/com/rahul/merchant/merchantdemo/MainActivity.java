package com.rahul.merchant.merchantdemo;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabase;
    private EditText coachingName, address, phone, city, faculty, noOfStudents, fee, courses, subjects;
    private Button submit, clear;
    LinearLayout rootLayout;
    boolean isValid = true;
    final int MANDATORY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setMandatoryTag();
        createDatabase();
        setListeners();
    }

    private void bindViews() {
        rootLayout = (LinearLayout) findViewById(R.id.root_layout);
        coachingName = (EditText) findViewById(R.id.coaching_name_ET);
        address = (EditText) findViewById(R.id.address_ET);
        phone = (EditText) findViewById(R.id.phone_ET);
        city = (EditText) findViewById(R.id.city_ET);
        faculty = (EditText) findViewById(R.id.faculty_name_ET);
        noOfStudents = (EditText) findViewById(R.id.no_of_student_ET);
        fee = (EditText) findViewById(R.id.fee_ET);
        courses = (EditText) findViewById(R.id.course_ET);
        subjects = (EditText) findViewById(R.id.subject_ET);
        submit = (Button) findViewById(R.id.submit_BTN);
        clear = (Button) findViewById(R.id.clear_BTN);
    }

    private void setMandatoryTag() {
        coachingName.setTag(MANDATORY);
        address.setTag(MANDATORY);
        phone.setTag(MANDATORY);
        city.setTag(MANDATORY);
        faculty.setTag(MANDATORY);
        courses.setTag(MANDATORY);
        subjects.setTag(MANDATORY);
        fee.setTag(MANDATORY);
    }

    private void setListeners() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                DataModel value = dataSnapshot.getValue(DataModel.class);
                Log.d(TAG, "Value is: ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isValid = true;
                if (!checkValidation(rootLayout)) return;
                DataModel model = new DataModel(getText(phone), getText(coachingName), getText(address), getText(city),
                        getText(faculty), getText(noOfStudents), getText(fee), getText(courses), getText(subjects));
                String key = mDatabase.push().getKey();
                mDatabase.child(key).setValue(model);
                showSnackBar();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData(rootLayout);
            }
        });
    }

    private void clearData(ViewGroup viewGroup) {
        coachingName.setText("");
        address.setText("");
        phone.setText("");
        city.setText("");
        faculty.setText("");
        courses.setText("");
        subjects.setText("");
        fee.setText("");
        noOfStudents.setText("");
    }

    private void showSnackBar() {
        Snackbar snackbar = Snackbar
                .make(rootLayout, "Updated Successfully", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private boolean checkValidation(ViewGroup view) {
        for(int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            if(child instanceof ViewGroup) {
                checkValidation((ViewGroup)child);
            }
            else if (isValid && child != null && child instanceof EditText && child.getTag() != null) {
                if ((int) child.getTag() == MANDATORY && TextUtils.isEmpty(getText(child))) {
                    child.requestFocus();
                    ((EditText) child).setError("Field cannot be empty");
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private String getText(View view) {
        if (view instanceof TextView)
            return ((TextView)view).getText().toString();
        else if (view instanceof EditText) {
            return ((EditText) view).getText().toString();
        }
        return "";
    }

    private void createDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }
}
