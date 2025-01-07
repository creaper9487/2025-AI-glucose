package rl.collab.diabeat

import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import rl.collab.diabeat.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val hostFile by lazy {
        File(filesDir, "host.txt")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(systemBars.left, systemBars.top, systemBars.right)
            insets
        }
        binding.navView.setupWithNavController(findNavController(R.id.container))

        if (hostFile.exists())
            hostFile.readText().split(" ").let {
                Client.hostC = it[0]
                Client.hostD = it[1]
            }

        setHost()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            setHost()
            return true
        }
        return false
    }

    private fun setHost() {
        val view = layoutInflater.inflate(R.layout.dialog_host, null)

        val cEt = view.findViewById<EditText>(R.id.host_c).apply { setText(Client.hostC) }
        val dEt = view.findViewById<EditText>(R.id.host_d).apply { setText(Client.hostD) }

        val dialog = customDialog(R.string.host, view)
        dialog.posBtn.setOnClickListener {
            val c = cEt.str
            val d = dEt.str

            if (c.isNotEmpty() && d.isNotEmpty()) {
                Client.hostC = c
                Client.hostD = d
                hostFile.writeText("$c $d")
                dialog.dismiss()
            }
        }
    }
}