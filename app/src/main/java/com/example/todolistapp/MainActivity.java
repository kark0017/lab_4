package com.example.todolistapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextTodo;
    private Button buttonAdd;
    private ListView listView;
    private List<TodoItem> todoItemList;
    private TodoListAdapter todoAdapter;

    private Switch switchUrgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        todoItemList = new ArrayList<>();


        Cursor cursor = db.query(DatabaseHelper.TABLE_TODO, null, null, null, null, null, null);
        printCursor(cursor);

        while (cursor.moveToNext()){
            String taskText = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TASK_TEXT));
            int isUrgent = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_URGENT));
            todoItemList.add(new TodoItem(taskText, isUrgent == 1));
        }



        editTextTodo = findViewById(R.id.editText);
        buttonAdd = findViewById(R.id.buttonAdd);
        listView = findViewById(R.id.listView);
        switchUrgent = findViewById(R.id.switchUrgent);


        //list of todo items
        todoItemList = new ArrayList<>();
        todoItemList.add(new TodoItem("Finish Lab 4", false));
        todoItemList.add(new TodoItem("Cook Dinner", true));
        todoItemList.add(new TodoItem("Brush Teeth", false));
        todoItemList.add(new TodoItem("Do Laundry", true));

        todoAdapter = new TodoListAdapter(this, todoItemList);
        listView.setAdapter(todoAdapter);


        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoText = editTextTodo.getText().toString().trim();
                Boolean switchValue = switchUrgent.isChecked();

                if (!todoText.isEmpty()){
                    TodoItem newTodoItem = new TodoItem(todoText, switchValue);

                    todoItemList.add(newTodoItem);
                    editTextTodo.setText("");
                    todoAdapter.notifyDataSetChanged();

                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_TASK_TEXT, todoText);
                    values.put(DatabaseHelper.COLUMN_IS_URGENT, switchValue ? 1 : 0);
                    db.insert("todos", null, values);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                builder.setTitle("Do you want to delete this?")
                        .setMessage("The selected row is: " + position)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                todoItemList.remove(position);
                                todoAdapter.notifyDataSetChanged();

                                DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                // Define the selection criteria and arguments
                                String selection = DatabaseHelper.COLUMN_ID + "=?";
                                String[] selectionArgs = {String.valueOf(id)};

                                // Delete the row from the database
                                db.delete(DatabaseHelper.TABLE_TODO, selection, selectionArgs);

                                db.close();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });
    }

    private void printCursor(Cursor cursor) {
        int columnCount = cursor.getColumnCount();
        String[] columnNames = cursor.getColumnNames();
        int resultCount = cursor.getCount();

        Log.d("Debug", "Number of columns: " + columnCount);
        Log.d("Debug", "Column names: " + Arrays.toString(columnNames));
        Log.d("Debug", "Number of results: " + resultCount);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            StringBuilder rowInfo = new StringBuilder("Row: ");
            for (int i = 0; i < columnCount; i++) {
                rowInfo.append(columnNames[i]).append("=").append(cursor.getString(i)).append(", ");
            }
            Log.d("CursorDebug", rowInfo.toString());
            cursor.moveToNext();
        }
    }


    public class TodoItem {
        private String taskText;
        private boolean isUrgent;

        public TodoItem(String taskText, boolean isUrgent) {
            this.taskText = taskText;
            this.isUrgent = isUrgent;
        }

        public String getTaskText() {
            return taskText;
        }

        public boolean isUrgent() {
            return isUrgent;
        }
    }

    public class TodoListAdapter extends BaseAdapter {
        private List<TodoItem> todoItemList;
        private Context context;

        public TodoListAdapter(Context context, List<TodoItem> todoItemList){
            this.context = context;
            this.todoItemList = todoItemList;
        }

        @Override
        public int getCount() {
            return todoItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return todoItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            }

            TextView todoTextView = convertView.findViewById(R.id.todoTextView);
            TodoItem todoItem = todoItemList.get(position);
            todoTextView.setText(todoItem.getTaskText());

            if(todoItem.isUrgent()){
                todoTextView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.red));
            }

            return convertView;
        }
    }

    public class DatabaseHelper extends SQLiteOpenHelper{

        private static final String DATABASE_NAME = "todo.db";
        private static final int DATABASE_VERSION = 1;

        // Table name and columns
        public static final String TABLE_TODO = "todos";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TASK_TEXT = "task_text";
        public static final String COLUMN_IS_URGENT = "is_urgent";

        private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_TODO + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_TEXT + " TEXT NOT NULL, " +
                COLUMN_IS_URGENT + " INTEGER DEFAULT 0);";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
            onCreate(db);

        }
    }
}

























