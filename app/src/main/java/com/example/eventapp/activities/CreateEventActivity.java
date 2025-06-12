package com.example.eventapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eventapp.models.ApiResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.data.DatabaseHelper;
import com.example.eventapp.api.EventApiService;
import com.example.eventapp.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;

@AndroidEntryPoint
public class CreateEventActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "CreateEventActivity";

    private ImageView eventImageView;
    private TextInputLayout titleLayout, descriptionLayout, locationLayout, priceLayout, ticketsLayout;
    private TextInputEditText titleInput, descriptionInput, locationInput, priceInput, ticketsInput, dateInput;
    private AutoCompleteTextView categoryInput;
    private MaterialButton uploadImageButton, createEventButton;
    private ProgressDialog progressDialog;

    private DatabaseHelper dbHelper;
    private Uri imageUri;
    private Date selectedDate;

    public static final String[] VALID_CATEGORIES = {"social", "business", "sports", "education", "other"};

    @Inject
    EventApiService eventApiService;

    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        dbHelper = new DatabaseHelper(this);

        // Initialize toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initializeViews();
        setupCategoryDropdown();
        setupDatePicker();
        setupImageUpload();
        setupCreateButton();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating event...");
        progressDialog.setCancelable(false);
    }

    private void initializeViews() {
        eventImageView = findViewById(R.id.event_image);
        titleLayout = findViewById(R.id.titleLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        locationLayout = findViewById(R.id.locationLayout);
        priceLayout = findViewById(R.id.priceLayout);
        ticketsLayout = findViewById(R.id.ticketsLayout);

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        locationInput = findViewById(R.id.locationInput);
        priceInput = findViewById(R.id.priceInput);
        ticketsInput = findViewById(R.id.ticketsInput);
        dateInput = findViewById(R.id.dateInput);
        categoryInput = findViewById(R.id.categoryInput);

        uploadImageButton = findViewById(R.id.upload_image_button);
        createEventButton = findViewById(R.id.create_event_button);
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, VALID_CATEGORIES);
        categoryInput.setAdapter(adapter);
    }

    private void setupDatePicker() {
        dateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        selectedDate = calendar.getTime();
                        dateInput.setText(String.format("%d/%d/%d", month + 1, dayOfMonth, year));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void setupImageUpload() {
        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                Glide.with(this).load(imageUri).centerCrop().into(eventImageView);
            }
        }
    }

    private void setupCreateButton() {
        createEventButton.setOnClickListener(v -> {
            if (validateInputs()) {
                progressDialog.show();
                saveEventWithImage();
            }
        });
    }

    private void saveEventWithImage() {
        try {
            // Get auth token
            String token = sessionManager.getToken();
            if (token == null) {
                Toast.makeText(this, "Please log in to create an event", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }

            // Format date for backend (ISO format)
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            String formattedDate = isoFormat.format(selectedDate);

            // Create individual form data parts instead of sending Event object
            RequestBody titlePart = RequestBody.create(MediaType.parse("text/plain"), titleInput.getText().toString());
            RequestBody descriptionPart = RequestBody.create(MediaType.parse("text/plain"), descriptionInput.getText().toString());
            RequestBody datePart = RequestBody.create(MediaType.parse("text/plain"), formattedDate);
            RequestBody locationPart = RequestBody.create(MediaType.parse("text/plain"), locationInput.getText().toString());
            RequestBody capacityPart = RequestBody.create(MediaType.parse("text/plain"), ticketsInput.getText().toString());
            RequestBody pricePart = RequestBody.create(MediaType.parse("text/plain"), priceInput.getText().toString());
            RequestBody categoryPart = RequestBody.create(MediaType.parse("text/plain"), categoryInput.getText().toString().toLowerCase());

            MultipartBody.Part imagePart = null;
            if (imageUri != null) {
                try {
                    String fileName = getFileName(imageUri);
                    ContentResolver contentResolver = getContentResolver();
                    RequestBody requestFile = RequestBody.create(
                            MediaType.parse(contentResolver.getType(imageUri)),
                            getBytes(contentResolver.openInputStream(imageUri))
                    );
                    imagePart = MultipartBody.Part.createFormData("image", fileName, requestFile);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing image", e);
                    Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return;
                }
            }

            // Make API call with individual parts
            Call<ApiResponse<Event>> call;
            if (imagePart != null) {
                call = eventApiService.createEventWithFormData(
                        "Bearer " + token,
                        titlePart,
                        descriptionPart,
                        datePart,
                        locationPart,
                        capacityPart,
                        pricePart,
                        categoryPart,
                        imagePart
                );
            } else {
                // Create event without image using the regular endpoint
                Event event = new Event(
                        titleInput.getText().toString(),
                        descriptionInput.getText().toString(),
                        selectedDate,
                        locationInput.getText().toString(),
                        Integer.parseInt(ticketsInput.getText().toString()),
                        Double.parseDouble(priceInput.getText().toString()),
                        categoryInput.getText().toString().toLowerCase()
                );
                call = eventApiService.createEvent("Bearer " + token, event);
            }

            call.enqueue(new Callback<ApiResponse<Event>>() {
                @Override
                public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                    progressDialog.dismiss();

                    Log.d(TAG, "Response code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response: " + errorBody);
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMessage = response.body() != null ?
                                response.body().getMessage() : "Failed to create event";
                        Toast.makeText(CreateEventActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                    progressDialog.dismiss();
                    Log.e(TAG, "Network error", t);
                    Toast.makeText(CreateEventActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Error creating event", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "image.jpg";
    }

    private void createEvent(String imagePath) {
        try {
            Event event = new Event(
                    titleInput.getText().toString(),
                    descriptionInput.getText().toString(),
                    selectedDate,
                    locationInput.getText().toString(),
                    Integer.parseInt(ticketsInput.getText().toString()),
                    Double.parseDouble(priceInput.getText().toString()),
                    categoryInput.getText().toString().toLowerCase()
            );

            String token = sessionManager.getToken();
            if (token == null) {
                Toast.makeText(this, "Please log in to create an event", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }

            eventApiService.createEvent("Bearer " + token, event).enqueue(new Callback<ApiResponse<Event>>() {
                @Override
                public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMessage = response.body() != null ?
                                response.body().getMessage() : "Failed to create event";
                        Toast.makeText(CreateEventActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(CreateEventActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (titleInput.getText().toString().trim().isEmpty()) {
            titleLayout.setError("Title is required");
            isValid = false;
        } else {
            titleLayout.setError(null);
        }

        if (descriptionInput.getText().toString().trim().isEmpty()) {
            descriptionLayout.setError("Description is required");
            isValid = false;
        } else {
            descriptionLayout.setError(null);
        }

        if (locationInput.getText().toString().trim().isEmpty()) {
            locationLayout.setError("Location is required");
            isValid = false;
        } else {
            locationLayout.setError(null);
        }

        if (priceInput.getText().toString().trim().isEmpty()) {
            priceLayout.setError("Price is required");
            isValid = false;
        } else {
            try {
                Double.parseDouble(priceInput.getText().toString());
                priceLayout.setError(null);
            } catch (NumberFormatException e) {
                priceLayout.setError("Invalid price format");
                isValid = false;
            }
        }

        if (ticketsInput.getText().toString().trim().isEmpty()) {
            ticketsLayout.setError("Number of tickets is required");
            isValid = false;
        } else {
            try {
                int tickets = Integer.parseInt(ticketsInput.getText().toString());
                if (tickets < 1) {
                    ticketsLayout.setError("Number of tickets must be at least 1");
                    isValid = false;
                } else {
                    ticketsLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                ticketsLayout.setError("Invalid number format");
                isValid = false;
            }
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (categoryInput.getText().toString().trim().isEmpty()) {
            categoryInput.setError("Category is required");
            isValid = false;
        } else {
            String category = categoryInput.getText().toString().toLowerCase();
            boolean validCategory = false;
            for (String validCat : VALID_CATEGORIES) {
                if (validCat.equals(category)) {
                    validCategory = true;
                    break;
                }
            }
            if (!validCategory) {
                categoryInput.setError("Please select a valid category");
                isValid = false;
            } else {
                categoryInput.setError(null);
            }
        }

        return isValid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}




//package com.example.eventapp.activities;
//
//import android.app.DatePickerDialog;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.ImageView;
//import android.widget.Toast;
//import java.io.IOException;
//import android.database.Cursor;
//import android.provider.OpenableColumns;
//import android.content.ContentResolver;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.widget.Toast;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.UUID;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.bumptech.glide.Glide;
//import com.example.eventapp.models.ApiResponse;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//import com.example.eventapp.R;
//import com.example.eventapp.models.Event;
//import com.example.eventapp.data.DatabaseHelper;
//import com.example.eventapp.api.EventApiService;
//import com.example.eventapp.utils.SessionManager;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import javax.inject.Inject;
//import dagger.hilt.android.AndroidEntryPoint;
//import org.json.JSONObject;
//import okhttp3.MediaType;
//import okhttp3.RequestBody;
//import okhttp3.MultipartBody;
//
//@AndroidEntryPoint
//public class CreateEventActivity extends AppCompatActivity {
//    private static final int PICK_IMAGE_REQUEST = 1;
//    private static final String TAG = "CreateEventActivity";
//
//    private ImageView eventImageView;
//    private TextInputLayout titleLayout, descriptionLayout, locationLayout, priceLayout, ticketsLayout;
//    private TextInputEditText titleInput, descriptionInput, locationInput, priceInput, ticketsInput, dateInput;
//    private AutoCompleteTextView categoryInput;
//    private MaterialButton uploadImageButton, createEventButton;
//    private ProgressDialog progressDialog;
//
//    private DatabaseHelper dbHelper;
//    private Uri imageUri;
//    private Date selectedDate;
//
//    public static final String[] VALID_CATEGORIES = {"social", "business", "sports", "education", "other"};
//
//    @Inject
//    EventApiService eventApiService;
//
//    @Inject
//    SessionManager sessionManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_create_event);
//
//        dbHelper = new DatabaseHelper(this);
//
//        // Initialize toolbar
//        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(v -> onBackPressed());
//
//        initializeViews();
//        setupCategoryDropdown();
//        setupDatePicker();
//        setupImageUpload();
//        setupCreateButton();
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Creating event...");
//        progressDialog.setCancelable(false);
//    }
//
//    private void initializeViews() {
//        eventImageView = findViewById(R.id.event_image);
//        titleLayout = findViewById(R.id.titleLayout);
//        descriptionLayout = findViewById(R.id.descriptionLayout);
//        locationLayout = findViewById(R.id.locationLayout);
//        priceLayout = findViewById(R.id.priceLayout);
//        ticketsLayout = findViewById(R.id.ticketsLayout);
//
//        titleInput = findViewById(R.id.titleInput);
//        descriptionInput = findViewById(R.id.descriptionInput);
//        locationInput = findViewById(R.id.locationInput);
//        priceInput = findViewById(R.id.priceInput);
//        ticketsInput = findViewById(R.id.ticketsInput);
//        dateInput = findViewById(R.id.dateInput);
//        categoryInput = findViewById(R.id.categoryInput);
//
//        uploadImageButton = findViewById(R.id.upload_image_button);
//        createEventButton = findViewById(R.id.create_event_button);
//    }
//
//    private void setupCategoryDropdown() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, VALID_CATEGORIES);
//        categoryInput.setAdapter(adapter);
//    }
//
//    private void setupDatePicker() {
//        dateInput.setOnClickListener(v -> {
//            Calendar calendar = Calendar.getInstance();
//            DatePickerDialog datePickerDialog = new DatePickerDialog(
//                    this,
//                    (view, year, month, dayOfMonth) -> {
//                        calendar.set(year, month, dayOfMonth);
//                        selectedDate = calendar.getTime();
//                        dateInput.setText(String.format("%d/%d/%d", month + 1, dayOfMonth, year));
//                    },
//                    calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH),
//                    calendar.get(Calendar.DAY_OF_MONTH)
//            );
//            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
//            datePickerDialog.show();
//        });
//    }
//
//    private void setupImageUpload() {
//        uploadImageButton.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(intent, PICK_IMAGE_REQUEST);
//        });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            imageUri = data.getData();
//            if (imageUri != null) {
//                Glide.with(this).load(imageUri).centerCrop().into(eventImageView);
//            }
//        }
//    }
//
//    private void setupCreateButton() {
//        createEventButton.setOnClickListener(v -> {
//            if (validateInputs()) {
//                progressDialog.show();
//                saveEventWithImage();
//            }
//        });
//    }
//
//    private void saveEventWithImage() {
//        if (imageUri != null) {
//            try {
//                String fileName = getFileName(imageUri);
//                ContentResolver contentResolver = getContentResolver();
//                RequestBody requestFile = RequestBody.create(
//                    MediaType.parse(contentResolver.getType(imageUri)),
//                    getBytes(contentResolver.openInputStream(imageUri))
//                );
//                MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", fileName, requestFile);
//
//                // Create Event object
//                Event event = new Event(
//                    titleInput.getText().toString(),
//                    descriptionInput.getText().toString(),
//                    selectedDate,
//                    locationInput.getText().toString(),
//                    Integer.parseInt(ticketsInput.getText().toString()),
//                    Double.parseDouble(priceInput.getText().toString()),
//                    categoryInput.getText().toString().toLowerCase()
//                );
//
//                // Get auth token
//                String token = sessionManager.getToken();
//                if (token == null) {
//                    Toast.makeText(this, "Please log in to create an event", Toast.LENGTH_SHORT).show();
//                    progressDialog.dismiss();
//                    return;
//                }
//
//                // Make API call with image and event object
//                eventApiService.createEventWithImage(
//                    "Bearer " + token,
//                    event,
//                    imagePart
//                ).enqueue(new Callback<ApiResponse<Event>>() {
//                    @Override
//                    public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
//                        progressDialog.dismiss();
//                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
//                            Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
//                            finish();
//                        } else {
//                            String errorMessage = response.body() != null ?
//                                response.body().getMessage() : "Failed to create event";
//                            Toast.makeText(CreateEventActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
//                        progressDialog.dismiss();
//                        Toast.makeText(CreateEventActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } catch (Exception e) {
//                progressDialog.dismiss();
//                Log.e(TAG, "Error creating event with image", e);
//                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            // Create event without image
//            createEvent(null);
//        }
//    }
//
//    private byte[] getBytes(InputStream inputStream) throws IOException {
//        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//        int bufferSize = 1024;
//        byte[] buffer = new byte[bufferSize];
//        int len;
//        while ((len = inputStream.read(buffer)) != -1) {
//            byteBuffer.write(buffer, 0, len);
//        }
//        return byteBuffer.toByteArray();
//    }
//
//    private String getFileName(Uri uri) {
//        String result = null;
//        if (uri.getScheme().equals("content")) {
//            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                }
//            }
//        }
//        if (result == null) {
//            result = uri.getPath();
//            int cut = result.lastIndexOf('/');
//            if (cut != -1) {
//                result = result.substring(cut + 1);
//            }
//        }
//        return result;
//    }
//
//    private void createEvent(String imagePath) {
//        try {
//            Event event = new Event(
//                titleInput.getText().toString(),
//                descriptionInput.getText().toString(),
//                selectedDate,
//                locationInput.getText().toString(),
//                Integer.parseInt(ticketsInput.getText().toString()),
//                Double.parseDouble(priceInput.getText().toString()),
//                categoryInput.getText().toString().toLowerCase()
//            );
//
//            String userId = sessionManager.getUserId();
//            if (userId == null) {
//                Toast.makeText(this, "Please log in to create an event", Toast.LENGTH_SHORT).show();
//                progressDialog.dismiss();
//                return;
//            }
//
//            String token = sessionManager.getToken();
//            if (token == null) {
//                Toast.makeText(this, "Please log in to create an event", Toast.LENGTH_SHORT).show();
//                progressDialog.dismiss();
//                return;
//            }
//
//            eventApiService.createEvent("Bearer " + token, event).enqueue(new Callback<ApiResponse<Event>>() {
//                @Override
//                public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
//                    progressDialog.dismiss();
//                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
//                        Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
//                        finish();
//                    } else {
//                        String errorMessage = response.body() != null ?
//                            response.body().getMessage() : "Failed to create event";
//                        Toast.makeText(CreateEventActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
//                    progressDialog.dismiss();
//                    Toast.makeText(CreateEventActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } catch (Exception e) {
//            progressDialog.dismiss();
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private boolean validateInputs() {
//        boolean isValid = true;
//
//        if (titleInput.getText().toString().trim().isEmpty()) {
//            titleLayout.setError("Title is required");
//            isValid = false;
//        } else {
//            titleLayout.setError(null);
//        }
//
//        if (descriptionInput.getText().toString().trim().isEmpty()) {
//            descriptionLayout.setError("Description is required");
//            isValid = false;
//        } else {
//            descriptionLayout.setError(null);
//        }
//
//        if (locationInput.getText().toString().trim().isEmpty()) {
//            locationLayout.setError("Location is required");
//            isValid = false;
//        } else {
//            locationLayout.setError(null);
//        }
//
//        if (priceInput.getText().toString().trim().isEmpty()) {
//            priceLayout.setError("Price is required");
//            isValid = false;
//        } else {
//            try {
//                Double.parseDouble(priceInput.getText().toString());
//                priceLayout.setError(null);
//            } catch (NumberFormatException e) {
//                priceLayout.setError("Invalid price format");
//                isValid = false;
//            }
//        }
//
//        if (ticketsInput.getText().toString().trim().isEmpty()) {
//            ticketsLayout.setError("Number of tickets is required");
//            isValid = false;
//        } else {
//            try {
//                Integer.parseInt(ticketsInput.getText().toString());
//                ticketsLayout.setError(null);
//            } catch (NumberFormatException e) {
//                ticketsLayout.setError("Invalid number format");
//                isValid = false;
//            }
//        }
//
//        if (selectedDate == null) {
//            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
//            isValid = false;
//        }
//
//        if (categoryInput.getText().toString().trim().isEmpty()) {
//            categoryInput.setError("Category is required");
//            isValid = false;
//        }
//
//        return isValid;
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//    }
//}