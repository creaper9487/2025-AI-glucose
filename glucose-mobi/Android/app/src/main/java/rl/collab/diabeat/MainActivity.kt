package rl.collab.diabeat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import rl.collab.diabeat.databinding.ActivityMainBinding
import rl.collab.diabeat.databinding.DialogHostBinding
import rl.collab.diabeat.frag.AccFrag.Companion.remePref

class MainActivity : AppCompatActivity() {
    private lateinit var hostPref: SharedPreferences
    private val hostStartup get() = hostPref.getBoolean("startup", true)
    private val hostAddr get() = hostPref.getString("addr", "192.168.0.0")!!

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            setHost()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)

        binding.apply {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(systemBars.left, systemBars.top, systemBars.right)
                insets
            }

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
            navView.setupWithNavController(navHostFragment.navController)
        }

        val masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        remePref = EncryptedSharedPreferences.create(
            applicationContext,
            "reme",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        hostPref = getSharedPreferences("host", Context.MODE_PRIVATE)
        if (hostStartup)
            setHost()
        else
            Client.resetRetro(hostAddr)
    }

    fun setHost() {
        val binding = DialogHostBinding.inflate(layoutInflater)

        binding.apply {
            startUpCb.isChecked = hostStartup

            val ets = arrayOf(hostA, hostB, hostC, hostD)
            val dialog = dialog("Host 設定", view = root)
            val posBtn = dialog.pos
            posBtn.setOnClickListener {
                toast("修改完成✅")
                dialog.dismiss()

                val addr = ets.joinToString(".") { it.str }
                hostPref.syncEdit {
                    putBoolean("startup", startUpCb.isChecked)
                    putString("addr", addr)
                }
                Client.resetRetro(addr)
            }

            val parts = hostAddr.split('.')
            val watcher = { _: Editable? ->
                posBtn.isEnabled = ets.all { it.str.isNotEmpty() && it.str.toInt() <= 255 }
            }
            for (i in 0..3)
                ets[i].apply {
                    setText(parts[i])
                    doAfterTextChanged(watcher)
                }
            hostD.setOnEditorActionListener { _, _, _ ->
                posBtn.callOnClick()
            }
        }
    }
}