package rl.collab.aiglucose

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
        setNavView()
    }

    private fun setNavView() {
        val navView = binding.navView
        val menu = navView.menu
        val navController = findNavController(R.id.container)
        navView.setupWithNavController(navController)

        var preId = -1
        val id2Item = mutableMapOf(
            R.id.recordFrag to menu.getItem(0),
            R.id.figureFrag to menu.getItem(1),
            R.id.accFrag to menu.getItem(2)
        )
        val id2DrawableIdPair = mutableMapOf(
            R.id.recordFrag to (R.drawable.record_outline to R.drawable.record_filled),
            R.id.figureFrag to (R.drawable.figure_outline to R.drawable.figure_filled),
            R.id.accFrag to (R.drawable.acc_outline to R.drawable.acc_filled)
        )
        navController.addOnDestinationChangedListener { _, dest, _ ->
            if (preId != -1)
                id2Item[preId]!!.setIcon(id2DrawableIdPair[preId]!!.first)
            id2Item[dest.id]!!.setIcon(id2DrawableIdPair[dest.id]!!.second)
            preId = dest.id
        }
    }
}
