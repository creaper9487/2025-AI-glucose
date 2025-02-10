package rl.collab.diabeat.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import rl.collab.diabeat.databinding.FragRecordBinding

class RecordFrag : Fragment() {
    private lateinit var binding: FragRecordBinding

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}