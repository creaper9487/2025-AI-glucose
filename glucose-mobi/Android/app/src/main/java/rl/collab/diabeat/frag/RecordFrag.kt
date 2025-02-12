package rl.collab.diabeat.frag

import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import rl.collab.diabeat.Client
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.FragRecordBinding
import rl.collab.diabeat.fmt
import rl.collab.diabeat.shortToast
import rl.collab.diabeat.str

class RecordFrag : Fragment() {
    lateinit var binding: FragRecordBinding

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val contentResolver = requireContext().contentResolver

        lateinit var filename: String
        var fileSize = 0L

        val cursor = contentResolver.query(uri!!, null, null, null, null)
        cursor?.use {
            cursor.moveToFirst()
            filename = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
        }

        shortToast("上傳 ${fileSize.fmt}")

        val byteArr = contentResolver.openInputStream(uri)!!.readBytes()
        val obj = byteArr.toRequestBody("image/*".toMediaType())
        val image = MultipartBody.Part.createFormData("image", filename, obj)
        Client.predict(this, image)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveBtn.setOnClickListener {
            if (AccFrag.token == null)
                shortToast("請先登入")
            else if (binding.glucoseEt.str.isEmpty())
                shortToast("血糖值不能為空")
            else {
                val obj = Request.Record(
                    binding.glucoseEt.str.toDouble(),
                    binding.carbohydrateEt.str.toDoubleOrNull(),
                    binding.exerciseEt.str.toDoubleOrNull(),
                    binding.insulinEt.str.toDoubleOrNull()
                )
                Client.postRecord(this, obj)
            }
        }

        binding.predictBtn.setOnClickListener {
            if (AccFrag.token == null)
                shortToast("請先登入")
            else
                pickImageLauncher.launch("image/*")
        }
    }
}