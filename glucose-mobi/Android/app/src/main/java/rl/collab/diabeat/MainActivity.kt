package rl.collab.diabeat

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import rl.collab.diabeat.databinding.ActivityMainBinding
import rl.collab.diabeat.databinding.DialogHostBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var hostFile: File

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

        hostFile = File(filesDir, "host.txt")
        if (hostFile.exists())
            for ((i, line) in hostFile.readLines().withIndex()) {
                if (i <= 3)
                    Client.host[i] = line
                else if (line.toBoolean())
                    setHost(false)
            }
        else
            setHost(false)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            setHost(true)
            return true
        }
        return false
    }

    private fun setHost(onKeyDown: Boolean) {
        val binding = DialogHostBinding.inflate(layoutInflater)

        binding.run {
            val ets = arrayOf(hostA, hostB, hostC, hostD)
            for (i in 0..3)
                ets[i].setText(Client.host[i])

            if (!hostFile.exists() || hostFile.readLines()[4].toBoolean())
                startUpCb.isChecked = true

            val dialog = viewDialog("Host", root, null)
            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.setOnClickListener {
                if (ets.any { it.str.isEmpty() || it.str.toInt() > 255 })
                    return@setOnClickListener

                for (i in 0..3)
                    Client.host[i] = ets[i].str

                hostFile.writeText(ets.joinToString("\n") { it.str } + "\n${startUpCb.isChecked}")
                dialog.dismiss()

                if (onKeyDown)
                    Client.resetRetro()
            }
        }
    }
}