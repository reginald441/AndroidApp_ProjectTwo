package com.example.reginaldkargboandroidapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUser = "";
    private static final int SMS_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        showLoginScreen();
    }

    private LinearLayout baseLayout(String titleText) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);
        scrollView.addView(layout);

        TextView title = new TextView(this);
        title.setText(titleText);
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        setContentView(scrollView);
        return layout;
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(16);
        label.setPadding(0, 16, 0, 6);
        return label;
    }

    private EditText field(String hint, int inputType) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setInputType(inputType);
        editText.setSingleLine(true);
        return editText;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private void showLoginScreen() {
        LinearLayout layout = baseLayout("Inventory Login");

        TextView intro = new TextView(this);
        intro.setText("Please log in or create a new account to manage inventory items.");
        intro.setTextSize(16);
        intro.setGravity(Gravity.CENTER);
        intro.setPadding(0, 0, 0, 20);
        layout.addView(intro);

        layout.addView(label("Username"));
        EditText usernameField = field("Enter username", InputType.TYPE_CLASS_TEXT);
        layout.addView(usernameField);

        layout.addView(label("Password"));
        EditText passwordField = field("Enter password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordField);

        Button loginButton = button("Log In");
        layout.addView(loginButton);

        Button createAccountButton = button("Create New Account");
        layout.addView(createAccountButton);

        loginButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.validateUser(username, password)) {
                currentUser = username;
                showInventoryScreen();
            } else {
                Toast.makeText(this, "Invalid login. Create an account first.", Toast.LENGTH_SHORT).show();
            }
        });

        createAccountButton.setOnClickListener(v -> showCreateAccountScreen());
    }

    private void showCreateAccountScreen() {
        LinearLayout layout = baseLayout("Create Account");

        layout.addView(label("New Username"));
        EditText usernameField = field("Create username", InputType.TYPE_CLASS_TEXT);
        layout.addView(usernameField);

        layout.addView(label("New Password"));
        EditText passwordField = field("Create password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordField);

        Button createButton = button("Save Account");
        layout.addView(createButton);

        Button backButton = button("Back to Login");
        layout.addView(backButton);

        createButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Both fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.createUser(username, password);
            if (success) {
                Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
                showLoginScreen();
            } else {
                Toast.makeText(this, "Username already exists.", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> showLoginScreen());
    }

    private void showInventoryScreen() {
        LinearLayout layout = baseLayout("Inventory Data Grid");

        TextView welcome = new TextView(this);
        welcome.setText("Logged in as: " + currentUser);
        welcome.setTextSize(16);
        welcome.setPadding(0, 0, 0, 20);
        layout.addView(welcome);

        layout.addView(label("Item Name"));
        EditText itemName = field("Example: Safety Gloves", InputType.TYPE_CLASS_TEXT);
        layout.addView(itemName);

        layout.addView(label("Quantity"));
        EditText quantity = field("Example: 25", InputType.TYPE_CLASS_NUMBER);
        layout.addView(quantity);

        layout.addView(label("Location"));
        EditText location = field("Example: Storage Room A", InputType.TYPE_CLASS_TEXT);
        layout.addView(location);

        Button addButton = button("Add Item");
        layout.addView(addButton);

        Button smsButton = button("SMS Notification Settings");
        layout.addView(smsButton);

        Button logoutButton = button("Log Out");
        layout.addView(logoutButton);

        TextView gridTitle = new TextView(this);
        gridTitle.setText("Current Inventory");
        gridTitle.setTextSize(20);
        gridTitle.setTypeface(null, Typeface.BOLD);
        gridTitle.setPadding(0, 30, 0, 10);
        layout.addView(gridTitle);

        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(true);
        layout.addView(tableLayout);
        loadInventoryTable(tableLayout);

        addButton.setOnClickListener(v -> {
            String name = itemName.getText().toString().trim();
            String qtyText = quantity.getText().toString().trim();
            String itemLocation = location.getText().toString().trim();

            if (name.isEmpty() || qtyText.isEmpty() || itemLocation.isEmpty()) {
                Toast.makeText(this, "Complete all item fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            int qty = Integer.parseInt(qtyText);
            dbHelper.addItem(name, qty, itemLocation);
            Toast.makeText(this, "Item added.", Toast.LENGTH_SHORT).show();
            showInventoryScreen();
        });

        smsButton.setOnClickListener(v -> showSmsScreen());
        logoutButton.setOnClickListener(v -> showLoginScreen());
    }

    private void loadInventoryTable(TableLayout tableLayout) {
        tableLayout.removeAllViews();

        TableRow header = new TableRow(this);
        header.addView(tableCell("Item", true));
        header.addView(tableCell("Qty", true));
        header.addView(tableCell("Location", true));
        header.addView(tableCell("Action", true));
        tableLayout.addView(header);

        Cursor cursor = dbHelper.getItems();
        if (cursor.getCount() == 0) {
            TableRow emptyRow = new TableRow(this);
            TextView empty = tableCell("No items added yet.", false);
            emptyRow.addView(empty);
            tableLayout.addView(emptyRow);
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));

            TableRow row = new TableRow(this);
            row.addView(tableCell(name, false));
            row.addView(tableCell(String.valueOf(quantity), false));
            row.addView(tableCell(location, false));

            Button updateButton = button("Update");
            updateButton.setOnClickListener(v -> {
                dbHelper.updateItem(id, name, quantity + 1, location);
                Toast.makeText(this, "Item updated. Quantity increased by 1.", Toast.LENGTH_SHORT).show();
                showInventoryScreen();
            });
            row.addView(updateButton);

            Button deleteButton = button("Delete");
            deleteButton.setOnClickListener(v -> {
                dbHelper.deleteItem(id);
                Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
                showInventoryScreen();
            });
            row.addView(deleteButton);
            tableLayout.addView(row);
        }
        cursor.close();
    }

    private TextView tableCell(String text, boolean bold) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setTextSize(14);
        cell.setPadding(8, 8, 8, 8);
        if (bold) {
            cell.setTypeface(null, Typeface.BOLD);
        }
        return cell;
    }

    private void showSmsScreen() {
        LinearLayout layout = baseLayout("SMS Notifications");

        TextView info = new TextView(this);
        info.setText("This screen allows the app to request SMS permission so inventory alerts could be sent when items are low or need attention.");
        info.setTextSize(16);
        info.setPadding(0, 0, 0, 20);
        layout.addView(info);

        TextView status = new TextView(this);
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        status.setText(granted ? "SMS Permission Status: Granted" : "SMS Permission Status: Not Granted");
        status.setTextSize(16);
        status.setPadding(0, 0, 0, 20);
        layout.addView(status);

        Button requestButton = button("Request SMS Permission");
        layout.addView(requestButton);

        Button backButton = button("Back to Inventory");
        layout.addView(backButton);

        requestButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
            } else {
                Toast.makeText(this, "SMS permission is already granted.", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> showInventoryScreen());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. App will still work without SMS alerts.", Toast.LENGTH_LONG).show();
            }
            showSmsScreen();
        }
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "project_two_inventory.db";
        private static final int DATABASE_VERSION = 1;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");
            db.execSQL("CREATE TABLE inventory (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, quantity INTEGER, location TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS inventory");
            onCreate(db);
        }

        public boolean createUser(String username, String password) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password", password);
            long result = db.insert("users", null, values);
            return result != -1;
        }

        public boolean validateUser(String username, String password) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ? AND password = ?", new String[]{username, password});
            boolean valid = cursor.getCount() > 0;
            cursor.close();
            return valid;
        }

        public void addItem(String name, int quantity, String location) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("quantity", quantity);
            values.put("location", location);
            db.insert("inventory", null, values);
        }
        public void updateItem(int id, String name, int quantity, String location) {

            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("quantity", quantity);
            values.put("location", location);

            db.update(
                    "inventory",
                    values,
                    "id = ?",
                    new String[]{String.valueOf(id)}
            );
        }
        public Cursor getItems() {
            SQLiteDatabase db = getReadableDatabase();
            return db.rawQuery("SELECT id, name, quantity, location FROM inventory ORDER BY id DESC", null);
        }

        public void deleteItem(int id) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete("inventory", "id = ?", new String[]{String.valueOf(id)});
        }
    }
}
