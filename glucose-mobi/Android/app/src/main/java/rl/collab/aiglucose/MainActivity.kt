package rl.collab.aiglucose

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import rl.collab.aiglucose.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var whichAccId = R.id.accFrag

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
        setNavigation()
    }

    private fun setNavigation() {
        navController = findNavController(R.id.container)
        binding.navView.setupWithNavController(navController)
        binding.navView.setOnItemSelectedListener {
            navController.navigate(if (it.itemId == R.id.accFrag) whichAccId else it.itemId)
            true
        }

        val navViewIds = listOf(R.id.recordFrag, R.id.figureFrag, R.id.accFrag)
        val id2Item = mutableMapOf<Int, MenuItem>()
        val id2DrawableIdPair = mutableMapOf<Int, Pair<Int, Int>>()
        val menu = binding.navView.menu
        for ((i, id) in navViewIds.withIndex()) {
            id2Item[id] = menu.getItem(i)
            id2DrawableIdPair[id] = when (id) {
                R.id.recordFrag -> R.drawable.record_outline to R.drawable.record_filled
                R.id.figureFrag -> R.drawable.figure_outline to R.drawable.figure_filled
                R.id.accFrag -> R.drawable.acc_outline to R.drawable.acc_filled
                else -> -1 to -1
            }
        }
        var preId = -1
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val id = dest.id
            if (id !in navViewIds) return@addOnDestinationChangedListener

            if (whichAccId == R.id.profileFrag && id == R.id.accFrag) {
                whichAccId = R.id.accFrag
            } else {
                if (preId != -1)
                    id2Item[preId]!!.setIcon(id2DrawableIdPair[preId]!!.first)
                id2Item[id]!!.setIcon(id2DrawableIdPair[id]!!.second)
                preId = id
            }
        }
    }

    fun accNavigate(resId: Int) {
        whichAccId = resId
        navController.navigate(whichAccId)
    }
}
