package com.example.todolistapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
            }else{
                todoTextView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.white));
            }

            return convertView;
        }
    }
}

























