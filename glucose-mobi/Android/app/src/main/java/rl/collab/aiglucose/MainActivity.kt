package rl.collab.aiglucose

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import rl.collab.aiglucose.databinding.ActivityMainBinding

fun ll(vararg msg: Any) = Log.d("666", msg.joinToString(" "))
fun tt(context: Context, vararg msg: Any) =
    Toast.makeText(context, msg.joinToString(" "), Toast.LENGTH_SHORT).show()

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNaviView: BottomNavigationView
    private lateinit var frags: Array<UniversalFrag>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        frags = arrayOf(RecordFrag(), FigureFrag(), MyFrag())
        transact {
            add(R.id.container, frags[0])
            add(R.id.container, frags[1])
            add(R.id.container, frags[2])
            hide(frags[1])
            hide(frags[2])
        }

        bottomNaviView = binding.bottomNavi
        bottomNaviView.setOnItemSelectedListener { item ->
            transact {
                hide(getCurFrag())
                show(getFragByItemId(item.itemId))
            }
            true
        }
    }

    private fun transact(action: FragmentTransaction.() -> FragmentTransaction) {
        supportFragmentManager.beginTransaction().action().commit()
    }

    private fun getCurFrag(): UniversalFrag {
        for (frag in frags)
            if (frag.isVisible)
                return frag
        return frags[0]  // should not happen
    }

    private fun getFragByItemId(itemId: Int): Fragment {
        for ((i, frag) in frags.withIndex())
            if (bottomNaviView.menu.getItem(i).itemId == itemId)
                return frag
        return frags[0] // should not happen
    }
}
