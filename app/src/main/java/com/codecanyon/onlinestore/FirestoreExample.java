package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.codecanyon.onlinestore.models.BookClass;

public class FirestoreExample extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String KEY_TITLE = "Title";
    private static final String KEY_DESCRIPTION = "Description";

    Button save, load, update, delete, deleteBook, addBook, loadBooks;
    TextView detail;
    EditText title, description;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private CollectionReference collectionReference;

    // Create a Firebase Snapshot Listener
    private ListenerRegistration fireStormListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        save = findViewById(R.id.save);
        update = findViewById(R.id.update);
        load = findViewById(R.id.load);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        detail = findViewById(R.id.detail);
        delete = findViewById(R.id.deleteDesc);
        deleteBook = findViewById(R.id.deleteDocument);
        loadBooks = findViewById(R.id.loadBooks);
        addBook = findViewById(R.id.addBook);

        // initiate document and collection references
        collectionReference = db.collection("Books");
        documentReference = collectionReference.document("Books List");

        save.setOnClickListener(view -> saveBooks());
        load.setOnClickListener(view -> loadBooks());
        update.setOnClickListener(view -> updateDescription());
        delete.setOnClickListener(view -> deleteDescription());
        deleteBook.setOnClickListener(view -> deleteBook());
        addBook.setOnClickListener(view -> addBook());
        loadBooks.setOnClickListener(view -> loadAllBooks());

    }

    private void addBook() {
        collectionReference.add(new BookClass(title.getText().toString(), description.getText().toString())).addOnSuccessListener(documentReference -> Log.d(TAG, documentReference.getId()));
    }

    private void loadAllBooks() {
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String data = "";

                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    BookClass book = queryDocumentSnapshot.toObject(BookClass.class);
                    book.setBookID(queryDocumentSnapshot.getId());

                    String bookID = book.getBookID();
                    String bookName = book.getBookName();
                    String bookDesc = book.getBookDescription();

                    data += "ID: " + bookID + "\nBookName: " + bookName + "\nBook Desc: " + bookDesc;
                }

                detail.setText(data);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fireStormListener = collectionReference.addSnapshotListener((value, error) -> {
            if (error != null){
                return;
            }
            String data = "";

            for (QueryDocumentSnapshot queryDocumentSnapshot: value){
                BookClass book = queryDocumentSnapshot.toObject(BookClass.class);
                book.setBookID(queryDocumentSnapshot.getId());
                String bookID = book.getBookID();
                String bookName = book.getBookName();
                String bookDesc = book.getBookDescription();

                data += "ID: " + bookID + "\nBookName: " + bookName + "\nBook Desc: " + bookDesc;
            }

            detail.setText(data);
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        fireStormListener.remove();
    }

    private void loadBooks() {
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                BookClass book = documentSnapshot.toObject(BookClass.class);

//                String title = documentSnapshot.getString(KEY_TITLE);
//                String description = documentSnapshot.getString(KEY_DESCRIPTION);
                detail.setText(book.getBookName() + "\n" + book.getBookDescription());
                Toast.makeText(FirestoreExample.this, "Load Success", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Load Success");
            }else {
                Toast.makeText(FirestoreExample.this, "Load Failed", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Load Failed");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(FirestoreExample.this, "Load Failed", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Load Failed");
        });
    }

    private void saveBooks(){
        String title = this.title.getText().toString();
        String description = this.description.getText().toString();

        // Create a document to save
//        Map<String, Object> bookDetail = new HashMap<>();
//        bookDetail.put(KEY_TITLE, title);
//        bookDetail.put(KEY_DESCRIPTION, description);

        BookClass book = new BookClass(title, description);

        db.collection("Books")
                .document()
                .set(book)
                .addOnFailureListener(e -> {
                    Toast.makeText(FirestoreExample.this, "Write Failed", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Write Failed");
                })
                .addOnSuccessListener(unused -> {
                    Toast.makeText(FirestoreExample.this, "Write Saved", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Write Success");
                });

    }

    private void updateDescription(){
        String description = detail.getText().toString().trim();
//        Map<String, Object> book = new HashMap<>();
//        book.put(KEY_DESCRIPTION, description);

//        documentReference.set(book, SetOptions.merge());

        documentReference.update(KEY_DESCRIPTION, description);
    }

    private void deleteDescription(){
        documentReference.update(KEY_DESCRIPTION, FieldValue.delete());
    }

    private void deleteBook(){
        documentReference.delete();
    }
}