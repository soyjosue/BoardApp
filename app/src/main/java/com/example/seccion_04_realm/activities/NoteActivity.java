package com.example.seccion_04_realm.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.seccion_04_realm.R;
import com.example.seccion_04_realm.adapters.NoteAdapter;
import com.example.seccion_04_realm.models.Board;
import com.example.seccion_04_realm.models.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;

public class NoteActivity extends AppCompatActivity implements RealmChangeListener<Board> {

    private ListView listView;
    private FloatingActionButton fab;

    private NoteAdapter adapter;
    private RealmList<Note> notes;
    private Realm realm;

    private int boardId;
    private Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        realm = Realm.getDefaultInstance();

        if(getIntent().getExtras() != null)
            boardId = getIntent().getExtras().getInt("id");

        board = realm.where(Board.class).equalTo("id", boardId).findFirst();
        board.addChangeListener(this);
        notes = board.getNotes();

        this.setTitle(board.getTitle());

        fab = (FloatingActionButton) findViewById(R.id.fabAddNote);
        listView = (ListView) findViewById(R.id.listViewNote);
        adapter = new NoteAdapter(this, notes, R.layout.list_view_note_item);

        listView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForCreatingNote("Add New Note", "Type a note for " + board.getTitle() + ".");
            }
        });
        registerForContextMenu(listView);
    }

    private void showAlertForCreatingNote(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(title != null) builder.setTitle(title);
        if(message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewNote);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String note = input.getText().toString().trim();
                if(note.length() > 0)
                    createNewNote(note);
                else
                    Toast.makeText(getApplicationContext(), "The note can't be empty", Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showAlertForEditingNote(String title, String message, Note note) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(title != null) builder.setTitle(title);
        if(message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewNote);
        input.setText(note.getDescription());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = input.getText().toString().trim();
                if (description.length() == 0)
                    Toast.makeText(getApplicationContext(), "The description can't be empty for to edit the note", Toast.LENGTH_LONG).show();
                else if(description.equals(note.getDescription()))
                    Toast.makeText(getApplicationContext(), "The description is the same than it was before",Toast.LENGTH_LONG).show();
                else
                    editNote(description, note);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /** CRUD ACTION **/
    private void createNewNote(String note) {
        realm.beginTransaction();

        Note _note = new Note(note);
        realm.copyToRealm(_note);
        board.getNotes().add(_note);

        realm.commitTransaction();
    }
    private void deleteNote(Note note) {
        realm.beginTransaction();
        note.deleteFromRealm();
        realm.commitTransaction();
    }
    private void editNote(String newDescription, Note note) {
        realm.beginTransaction();
        note.setDescription(newDescription);
        realm.copyToRealmOrUpdate(note);
        realm.commitTransaction();
    }
    private void deleteAll() {
        realm.beginTransaction();
        board.getNotes().deleteAllFromRealm();
        realm.commitTransaction();
    }

    /* Listening */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_note:
                deleteAll();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(notes.get(info.position).getDescription());
        getMenuInflater().inflate(R.menu.context_menu_note_activity, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_note:
                deleteNote(notes.get(info.position));
                return true;
            case R.id.edit_note:
                showAlertForEditingNote("Edit Note", "Change the description of the note.", notes.get(info.position));
                return true;
            default: return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onChange(Board board) {
        adapter.notifyDataSetChanged();
    }
}