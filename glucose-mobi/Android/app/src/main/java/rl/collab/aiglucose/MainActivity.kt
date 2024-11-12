package rl.collab.aiglucose

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import rl.collab.aiglucose.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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
    }

    fun customDialog(titleStrRes: Int, view: View): AlertDialog =
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(getString(titleStrRes))
            .setView(view)
            .setPositiveButton(getString(R.string.ok), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
}

fun AlertDialog.setPosBtnOnClick(l: View.OnClickListener) = getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(l)