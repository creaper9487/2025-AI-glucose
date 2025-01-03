package rl.collab.aiglucose.onClick

import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import rl.collab.aiglucose.frag.AccFrag

class GoogleSignInBtnOnClick(accFrag: AccFrag) : View.OnClickListener {
    private lateinit var client: GoogleSignInClient

    override fun onClick(p0: View?) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("")
            .requestEmail()
            .build()
    }
}