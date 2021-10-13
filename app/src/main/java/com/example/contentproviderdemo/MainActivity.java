package com.example.contentproviderdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView txtRes;
    EditText edtName,edtGrade,edtUpdateId,edtUpdateName,edtUpdateGrade,edtDeleteId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtRes = findViewById(R.id.txtRes);
        edtName = findViewById(R.id.edtName);
        edtGrade = findViewById(R.id.edtGrade);
        edtUpdateId = findViewById(R.id.edtUpdateId);
        edtUpdateName = findViewById(R.id.edtUpdateName);
        edtUpdateGrade = findViewById(R.id.edtUpdateGrade);
        //edtDeleteId = findViewById(R.id.edtDeleteId);
    }

    public void clickToAdd(View view) {
        ContentValues values = new ContentValues();
        values.put(StudentProvider.NAME, edtName.getText().toString());
        values.put(StudentProvider.GRADE, edtGrade.getText().toString());
        System.out.println(StudentProvider.CONTENT_URI);
        getContentResolver().insert(StudentProvider.CONTENT_URI, values);
        Toast.makeText(getBaseContext(), "New Record Inserted", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("Range")
    public void clickToShow(View view) {
        Uri students = StudentProvider.CONTENT_URI;
        Cursor cursor = getContentResolver().query(students,null,null,null,"name");
        if(cursor.moveToFirst()){
            String result = "All Retrieve Students: \n";
            do{
                int id = cursor.getColumnIndex(StudentProvider._ID);
                int name = cursor.getColumnIndex(StudentProvider.NAME);
                int grade = cursor.getColumnIndex(StudentProvider.GRADE);
                result += cursor.getString(id) + " - " +
                        cursor.getString(name) + " - " +
                        cursor.getString(grade) + "\n";
            }while(cursor.moveToNext());
            txtRes.setText(result);
        }
    }

    public void clickToUpdate(View view) {
        ContentValues values = new ContentValues();
        values.put(StudentProvider._ID, ((EditText) findViewById(R.id.edtUpdateId)).getText().toString());
        values.put(StudentProvider.NAME, ((EditText) findViewById(R.id.edtUpdateName)).getText().toString());
        values.put(StudentProvider.GRADE, ((EditText) findViewById(R.id.edtUpdateGrade)).getText().toString());
        getContentResolver().update(StudentProvider.CONTENT_URI,values,StudentProvider._ID + " = ? " ,new String[]{edtUpdateId.getText().toString()});
        Toast.makeText(getBaseContext(), "Updated", Toast.LENGTH_LONG).show();
    }

    /*public void clickToDelete(View view) {
        ContentValues values = new ContentValues();
        values.put(StudentProvider._ID, ((EditText) findViewById(R.id.edtDeleteId)).getText().toString());
        getContentResolver().delete(StudentProvider.CONTENT_URI,StudentProvider._ID + " = ? " ,new String[]{edtDeleteId.getText().toString()});
        Toast.makeText(getBaseContext(), "Deleted", Toast.LENGTH_LONG).show();
    }*/
}