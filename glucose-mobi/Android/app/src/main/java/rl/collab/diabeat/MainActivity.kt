package rl.collab.diabeat

import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
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

    fun setHost(onKeyDown: Boolean) {
        val binding = DialogHostBinding.inflate(layoutInflater)

        binding.run {
            val ets = arrayOf(hostA, hostB, hostC, hostD)
            val dialog = viewDialog("Host", root)
            val posBtn = dialog.pos()
            posBtn.setOnClickListener {
                toast("修改完成✅")

                for (i in 0..3)
                    Client.host[i] = ets[i].str

                hostFile.writeText(ets.joinToString("\n") { it.str } + "\n${startUpCb.isChecked}")
                dialog.dismiss()

                if (onKeyDown)
                    Client.resetRetro()
            }

            val watcher = { _: Editable? ->
                posBtn.isEnabled = ets.all { it.str.isNotEmpty() && it.str.toInt() <= 255 }
            }
            for (i in 0..3)
                ets[i].run {
                    setText(Client.host[i])
                    doAfterTextChanged(watcher)
                }
            hostD.setOnEditorActionListener { _, _, _ ->
                posBtn.callOnClick()
            }

            if (!hostFile.exists() || hostFile.readLines()[4].toBoolean())
                startUpCb.isChecked = true
        }
    }
}