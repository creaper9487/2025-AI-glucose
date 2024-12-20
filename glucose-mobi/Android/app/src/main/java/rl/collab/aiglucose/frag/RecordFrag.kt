package rl.collab.aiglucose.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import rl.collab.aiglucose.databinding.FragRecordBinding

class RecordFrag : Fragment() {
    private lateinit var binding: FragRecordBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragRecordBinding.inflate(inflater, container, false)
        return binding.root
    }
}