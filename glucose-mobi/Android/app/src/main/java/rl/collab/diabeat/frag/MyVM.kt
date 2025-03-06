package rl.collab.diabeat.frag

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel

class MyVM : ViewModel() {
    lateinit var remePref: SharedPreferences

    var acc: String? = null
    var access: String? = null
    var refresh: String? = null
}