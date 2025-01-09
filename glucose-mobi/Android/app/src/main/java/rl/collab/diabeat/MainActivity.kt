package rl.collab.diabeat

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
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
    private val hostFile by lazy { File(filesDir, "host.txt") }

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
            for ((i, line) in hostFile.readLines().withIndex()) {
                if (i <= 3)
                    Client.host[i] = line
                else if (line.toBoolean())
                    setHost()
            }
        else
            setHost()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            setHost()
            return true
        }
        return false
    }

    private fun setHost() {
        val view = layoutInflater.inflate(R.layout.dialog_host, null)

        val ids = arrayOf(R.id.host_a, R.id.host_b, R.id.host_c, R.id.host_d)
        val ets = arrayListOf<EditText>()
        for ((id, hostI) in ids.zip(Client.host))
            ets.add(view.findViewById<EditText>(id).apply { setText(hostI) })

        val startUpCb = view.findViewById<CheckBox>(R.id.start_up_cb).apply {
            if (!hostFile.exists() || hostFile.readLines()[4].toBoolean())
                isChecked = true
        }

        val dialog = customDialog(R.string.host, view)
        dialog.posBtn.setOnClickListener {
            if (ets.any { it.str.isEmpty() || it.str.toInt() > 255 })
                return@setOnClickListener

            for ((i, et) in ets.withIndex())
                Client.host[i] = et.str

            hostFile.writeText(ets.joinToString("\n") { it.str } + "\n${startUpCb.isChecked}")
            dialog.dismiss()
        }

        ets[3].setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE && dialog.posBtn.isEnabled) {
                dialog.posBtn.callOnClick()
                true
            } else
                false
        }
    }
}