package com.sargent.mark.todolist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;


import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.DBHelper;

public class MainActivity extends AppCompatActivity implements AddToDoFragment.OnDialogCloseListener, UpdateToDoFragment.OnUpdateDialogCloseListener{

    private RecyclerView rv;
    private FloatingActionButton button;
    private DBHelper helper;
    private Cursor cursor;
    private SQLiteDatabase db;
    ToDoListAdapter adapter;
    private final String TAG = "mainactivity";
    private Spinner spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "oncreate called in main activity");
        button = (FloatingActionButton) findViewById(R.id.addToDo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                AddToDoFragment frag = new AddToDoFragment();
                frag.show(fm, "addtodofragment");
            }
        });
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        spinner = (Spinner) findViewById(R.id.spinner);


//        String task = String.valueOf(taskTextView.getText());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String text = spinner.getSelectedItem().toString();    // storing the value of the selected category in a String variable
                Cursor data =  getValues(Contract.TABLE_TODO.TABLE_NAME,new String[]{Contract.TABLE_TODO._ID,Contract.TABLE_TODO.COLUMN_NAME_SPINNER_VALUE,
                        Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE},
                        Contract.TABLE_TODO.COLUMN_NAME_SPINNER_VALUE + "='" + text + "'", null, null, null); // adding the spinner value to the table

                adapter = new ToDoListAdapter(data, new ToDoListAdapter.ItemClickListener() {

                    @Override
                    public void onItemClick(int pos, String description, String duedate,String spinnerValue, long id) {
                        Log.d(TAG, "item click id: " + id);
                        String[] dateInfo = duedate.split("-");
                        int year = Integer.parseInt(dateInfo[0].replaceAll("\\s",""));
                        int month = Integer.parseInt(dateInfo[1].replaceAll("\\s",""));
                        int day = Integer.parseInt(dateInfo[2].replaceAll("\\s",""));

                        FragmentManager fm = getSupportFragmentManager();

                        UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description, spinnerValue, id); // also adding the spinner value to the to do fragment when an existing to do is clicked
                        frag.show(fm, "updatetodofragment");
                    }
                });

                rv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /*Added a checkbox to do a strike through when a checkbox is check marked*/
    public void taskDone(View view) {
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.description);
        if(((CheckBox)view).isChecked()){
            taskTextView.setPaintFlags(taskTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            taskTextView.setPaintFlags(taskTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
//        String task = String.valueOf(taskTextView.getText());

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (db != null) db.close();
        if (cursor != null) cursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();
        cursor = getAllItems(db);

        adapter = new ToDoListAdapter(cursor, new ToDoListAdapter.ItemClickListener() {

            @Override
            public void onItemClick(int pos, String description, String duedate,String spinnerValue, long id) {
                Log.d(TAG, "item click id: " + id);
                String[] dateInfo = duedate.split("-");
                int year = Integer.parseInt(dateInfo[0].replaceAll("\\s",""));
                int month = Integer.parseInt(dateInfo[1].replaceAll("\\s",""));
                int day = Integer.parseInt(dateInfo[2].replaceAll("\\s",""));

                FragmentManager fm = getSupportFragmentManager();

                UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description, spinnerValue, id);
                frag.show(fm, "updatetodofragment");
            }
        });

        rv.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                Log.d(TAG, "passing id: " + id);
                removeToDo(db, id);
                adapter.swapCursor(getAllItems(db));  //  removed this line so that to dos from other categories don't pop up after deleting a to do

            }
        }).attachToRecyclerView(rv);
    }

    @Override
    public void closeDialog(int year, int month, int day, String description,String SpinnerValue) {
        addToDo(db, description, SpinnerValue, formatDate(year, month, day));
        cursor = getAllItems(db);
        adapter.swapCursor(cursor);
    }

    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }



    private Cursor getAllItems(SQLiteDatabase db) {
        return db.query(
                Contract.TABLE_TODO.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE
            //    Contract.TABLE_TODO.COLUMN_NAME_SPINNER_VALUE

        );
    }

    public Cursor getValues(String tableName, String[] colNames, String where, String groupBy, String having, String orderBY) throws SQLException
    {
        Cursor mCursor = null;
        try {
            SQLiteDatabase sqldb = db;
            mCursor = sqldb.query(tableName,colNames,where,null,groupBy,having,orderBY);

            if (mCursor != null)
            {
                mCursor.moveToFirst();
            }
        } catch (Exception e)
        {
            //Toast.makeText(m_context, e.toString(), 0);
        }
        return mCursor;
    }

    /*Adding all the values that the user has given to add a to do and  updating the database*/
    private long addToDo(SQLiteDatabase db, String description,String SpinnerValue, String duedate) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_SPINNER_VALUE,SpinnerValue);
        return db.insert(Contract.TABLE_TODO.TABLE_NAME, null, cv);

    }

    private boolean removeToDo(SQLiteDatabase db, long id) {
        Log.d(TAG, "deleting id: " + id);
        return db.delete(Contract.TABLE_TODO.TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
    }


    private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description, String SpinnerValue, long id){

        String duedate = formatDate(year, month - 1, day);

        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_SPINNER_VALUE,SpinnerValue);

        return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
    }

    @Override
    public void closeUpdateDialog(int year, int month, int day, String description, String SpinnerValue, long id) {
        updateToDo(db, year, month, day, description,SpinnerValue, id);
        adapter.swapCursor(getAllItems(db));
    }
}
