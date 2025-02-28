package rl.collab.diabeat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import rl.collab.diabeat.databinding.ActivityMainBinding
import rl.collab.diabeat.databinding.DialogHostBinding

class MainActivity : AppCompatActivity() {
    private lateinit var hostPref: SharedPreferences
    private val hostStartup get() = hostPref.getBoolean("startup", true)

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            setHost()
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)

        binding.run {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(systemBars.left, systemBars.top, systemBars.right)
                insets
            }
            navView.setupWithNavController(findNavController(R.id.container))
        }

        hostPref = getSharedPreferences("host", Context.MODE_PRIVATE)
        if (hostStartup)
            setHost()
    }

    fun setHost() {
        val binding = DialogHostBinding.inflate(layoutInflater)

        binding.run {
            startUpCb.isChecked = hostStartup

            val ets = arrayOf(hostA, hostB, hostC, hostD)
            val dialog = viewDialog("Host 設定", root)
            val posBtn = dialog.pos
            posBtn.setOnClickListener {
                toast("修改完成 ✅")
                dialog.dismiss()

                val addr = ets.joinToString(".") { it.str }
                hostPref.edit(true) {
                    putBoolean("startup", startUpCb.isChecked)
                    putString("addr", addr)
                }
                Client.resetRetro(addr)
            }

            val parts = hostPref.getString("addr", Client.DEFAULT_ADDR)!!.split('.')
            val watcher = { _: Editable? ->
                posBtn.isEnabled = ets.all { it.str.isNotEmpty() && it.str.toInt() <= 255 }
            }
            for (i in 0..3)
                ets[i].run {
                    setText(parts[i])
                    doAfterTextChanged(watcher)
                }
            hostD.setOnEditorActionListener { _, _, _ ->
                posBtn.callOnClick()
            }
        }
    }
}