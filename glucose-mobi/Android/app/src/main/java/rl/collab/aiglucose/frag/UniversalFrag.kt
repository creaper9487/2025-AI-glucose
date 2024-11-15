package rl.collab.aiglucose.frag

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import rl.collab.aiglucose.MainActivity

abstract class UniversalFrag : Fragment() {
    protected lateinit var ma: MainActivity

    abstract fun update()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ma = context as MainActivity
        view.setOnClickListener {
            val inputMethodManager = ma.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}