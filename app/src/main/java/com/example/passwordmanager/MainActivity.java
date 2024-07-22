package com.example.passwordmanager;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper db;
    ListView listView;
    Button addEntry, recycleBin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        listView = findViewById(R.id.listView);
        addEntry = findViewById(R.id.addEntry);
        recycleBin = findViewById(R.id.recycleBin);

        loadPasswords();

        addEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEntryDialog();
            }
        });

        recycleBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecycleBinDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditEntryDialog((int) id);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteEntry((int) id);
                return true;
            }
        });
    }

    private void loadPasswords() {
        Cursor cursor = db.getPasswords();
        String[] from = {"username", "password", "url"};
        int[] to = {R.id.username, R.id.password, R.id.url};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.password_item, cursor, from, to, 0);
        listView.setAdapter(adapter);
    }

    private void showAddEntryDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_entry, null);
        final EditText username = dialogView.findViewById(R.id.username);
        final EditText password = dialogView.findViewById(R.id.password);
        final EditText url = dialogView.findViewById(R.id.url);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Add Entry")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String user = username.getText().toString().trim();
                        String pass = password.getText().toString().trim();
                        String website = url.getText().toString().trim();
                        if (!user.equals("") && !pass.equals("") && !website.equals("")) {
                            long result = db.addPassword(user, pass, website);
                            if (result != -1) {
                                Toast.makeText(MainActivity.this, "Entry added successfully", Toast.LENGTH_SHORT).show();
                                loadPasswords();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to add entry", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showEditEntryDialog(final int id) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_entry, null);
        final EditText username = dialogView.findViewById(R.id.username);
        final EditText password = dialogView.findViewById(R.id.password);
        final EditText url = dialogView.findViewById(R.id.url);

        Cursor cursor = db.getPassword(id);
        if (cursor != null && cursor.moveToFirst()) {
            username.setText(cursor.getString(cursor.getColumnIndex("username")));
            password.setText(cursor.getString(cursor.getColumnIndex("password")));
            url.setText(cursor.getString(cursor.getColumnIndex("url")));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Edit Entry")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String user = username.getText().toString().trim();
                        String pass = password.getText().toString().trim();
                        String website = url.getText().toString().trim();
                        if (!user.equals("") && !pass.equals("") && !website.equals("")) {
                            db.updatePassword(id, user, pass, website);
                            Toast.makeText(MainActivity.this, "Entry updated successfully", Toast.LENGTH_SHORT).show();
                            loadPasswords();
                        } else {
                            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private void deleteEntry(final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deletePassword(id);
                        Toast.makeText(MainActivity.this, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                        loadPasswords();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showRecycleBinDialog() {
        Cursor cursor = db.getDeletedPasswords();
        String[] from = {"username", "password", "url"};
        int[] to = {R.id.username, R.id.password, R.id.url};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.password_item, cursor, from, to, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recycle Bin")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cursor.moveToPosition(which);
                        final int id = cursor.getInt(cursor.getColumnIndex("id"));
                        db.restorePassword(id);
                        Toast.makeText(MainActivity.this, "Entry restored successfully", Toast.LENGTH_SHORT).show();
                        loadPasswords();
                    }
                })
                .setNegativeButton("Close", null)
                .create()
                .show();
    }
}
