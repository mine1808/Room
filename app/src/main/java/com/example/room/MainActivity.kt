package com.example.room

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.room.database.Note
import com.example.room.database.NoteDao
import com.example.room.database.NoteRoomDatabase
import com.example.room.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mNotesDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int=0
    private var noteToDelete: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        mNotesDao = db!!.noteDao()!!

        with(binding){
            btnAdd.setOnClickListener(View.OnClickListener {
                insert(
                    Note(
                        title = edtTitle.text.toString(),
                        description = edtDesc.text.toString(),
                        date = edtDate.text.toString()
                    )
                )
                setEmptyField()
            })
            btnUpdate.setOnClickListener{
                update(
                    Note(
                        id = updateId,
                        title = edtTitle.getText().toString(),
                        description = edtDesc.getText().toString(),
                        date = edtDate.getText().toString()
                    )
                )
                updateId = 0
                setEmptyField()
            }
            btnDelete.setOnClickListener{
                // Assuming you have the note you want to delete stored in a variable called "noteToDelete"
                noteToDelete?.let {
                    delete(it)
                    noteToDelete = null // Reset the noteToDelete after deletion
                    setEmptyField()
                }
            }

            listView.setOnItemClickListener { adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Note
                updateId = item.id
                edtTitle.setText(item.title)
                edtDesc.setText(item.description)
                edtDate.setText(item.date)
                noteToDelete = item
            }
            listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { adapterView, _, i, _ ->
                    val item = adapterView.adapter.getItem(i) as Note
                    delete(item)
                    true
                }
        }
    }
    private fun getAllNotes() {
        mNotesDao.allNotes.observe(this) { notes ->
            val adapter: ArrayAdapter<Note> = ArrayAdapter<Note>(
                this,
                android.R.layout.simple_list_item_1, notes
            )
            binding.listView.adapter = adapter
        }
    }
    private fun insert(note: Note) {
        executorService.execute { mNotesDao.insert(note) }
    }
    private fun delete(note: Note) {
        executorService.execute { mNotesDao.delete(note) }
    }
    private fun update(note: Note) {
        executorService.execute { mNotesDao.update(note) }
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    private fun setEmptyField() {
        with(binding){
            edtTitle.setText("")
            edtDesc.setText("")
            edtDate.setText("")
        }
    }
}