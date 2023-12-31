package d.tmesaric.jadrijazadatak

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import d.tmesaric.jadrijazadatak.domain.model.Zadatak
import d.tmesaric.jadrijazadatak.presentation.ZadatakEvent
import d.tmesaric.jadrijazadatak.presentation.ZadatakViewModel
import d.tmesaric.jadrijazadatak.DetailsActivity
import d.tmesaric.jadrijazadatak.data.ZadatakDB
import d.tmesaric.jadrijazadatak.presentation.ZadatakState
import d.tmesaric.jadrijazadatak.presentation.recycler_view.ZadatakAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: ZadatakViewModel by viewModels()
    private lateinit var adapter: ZadatakAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rvZadatak = findViewById<RecyclerView>(R.id.rvZadatak)
        val btnAddZadatak = findViewById<Button>(R.id.btnAddZadatak)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                setupRecyclerView(state)
                rvZadatak.adapter = adapter
            }
        }

        rvZadatak.layoutManager = LinearLayoutManager(this)

        btnAddZadatak.setOnClickListener {
            showPopup(this, adapter)
        }
    }

    private fun setupRecyclerView(state: ZadatakState) {
        adapter = ZadatakAdapter(
            state.zadaci,
            { zadatak ->
                val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                intent.putExtra("zadatak", zadatak)
                startActivity(intent)
            },

            { zadatak -> viewModel.onEvent(ZadatakEvent.DeleteZadatak(zadatak)) },
        )
    }

    private fun showPopup(context: Context, adapter: ZadatakAdapter) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.popup, null)

        val etTitle: EditText = dialogView.findViewById(R.id.etTitle)
        val etContent: EditText = dialogView.findViewById(R.id.etContent)

        builder.setView(dialogView)
            .setPositiveButton("Submit") { dialog, which ->

                val title = etTitle.text.toString()
                val content = etContent.text.toString()
                val zadatak = Zadatak(null, title, content, System.currentTimeMillis(), false)
                adapter.zadaci = adapter.zadaci + zadatak
                viewModel.onEvent(ZadatakEvent.AddZadatak(zadatak))
                adapter.notifyItemInserted(adapter.zadaci.size - 1)

            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}