package com.sargent.mark.todolist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.ToDoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ItemHolder> {

    private Cursor cursor;
    private ItemClickListener listener;
    private String TAG = "todolistadapter";
    private Context context; // creating a Context object

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        this.context = context;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public interface ItemClickListener {
        void onItemClick(int pos, String description, String duedate,String SpinnerValue, long id);
    }

    public ToDoListAdapter(Cursor cursor, ItemClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    public void swapCursor(Cursor newCursor){
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
        TextView descr;
        TextView due;
        String duedate;
        String description;
        String SpinnerValue;
        CheckBox checkBox;
        long id;


        ItemHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);
            checkBox =(CheckBox) view.findViewById(R.id.check);
            view.setOnClickListener(this);
            checkBox.setOnCheckedChangeListener(this);
        }

        public void bind(ItemHolder holder, int pos) {
            cursor.moveToPosition(pos);
            id = cursor.getLong(cursor.getColumnIndex(Contract.TABLE_TODO._ID));
            Log.d(TAG, "deleting id: " + id);

            /*retrieving the information from the databsae for the spinner values*/
            duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
            description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));
            SpinnerValue = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_SPINNER_VALUE));
            descr.setText(description);
            due.setText(duedate);
            holder.itemView.setTag(id);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            listener.onItemClick(pos, description, duedate, SpinnerValue, id);
        }


        /*Created an onCheckedChanged method to get create an on strike and red color when the to do is marked
        *as done and we switch it back to normal text by adding black color to the unchecked view of the Recycler View next to the checkbox*/
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            if(b){
                Log.d("Testing","duedate:"+duedate);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date createdDate = format.parse(duedate);
                    System.out.println(createdDate);
                    Date presentDate = new Date();
                    Log.d("Testing","dateCheck:"+presentDate.after(createdDate));

                    if(presentDate.after(createdDate)){
                        descr.setPaintFlags(descr.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        descr.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    }
                    else{
                        descr.setPaintFlags(descr.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                        descr.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                    }
                } catch (Exception e) {
                    Log.d("Testing","Exception :"+e);
                }
            }
            else{
                descr.setPaintFlags(descr.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                descr.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            }

        }
    }

}
