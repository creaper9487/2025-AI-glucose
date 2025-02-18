package rl.collab.diabeat.frag

import android.app.AlertDialog
import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import rl.collab.diabeat.Client
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.FragRecordBinding
import rl.collab.diabeat.fmt
import rl.collab.diabeat.shortToast
import rl.collab.diabeat.str
import java.io.File

class RecordFrag : Fragment() {
    lateinit var binding: FragRecordBinding
    private lateinit var contentResolver: ContentResolver
    private lateinit var takePicResult: Pair<String, Uri>

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            lateinit var filename: String
            var fileSize = 0L

            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                cursor.moveToFirst()
                filename = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
            }

            shortToast("上傳 ${fileSize.fmt}")

            val byteArr = contentResolver.openInputStream(uri)!!.readBytes()
            val obj = byteArr.toRequestBody("image/*".toMediaType())
            val image = MultipartBody.Part.createFormData("image", filename, obj)
            Client.predictCarbohydrate(this, image)
        }
    }

    private val takePicLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val byteArr = contentResolver.openInputStream(takePicResult.second)!!.readBytes()
            val obj = byteArr.toRequestBody("image/jpeg".toMediaType())
            val image = MultipartBody.Part.createFormData("image", takePicResult.first, obj)
            Client.predictCarbohydrate(this, image)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentResolver = requireContext().contentResolver

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
            else {
                val dialogView = layoutInflater.inflate(R.layout.dialog_src, null)
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("選擇來源")
                    .setView(dialogView)
                    .show()

                dialogView.findViewById<TextView>(R.id.photo_tv).setOnClickListener {
                    pickImageLauncher.launch("image/*")
                    dialog.dismiss()
                }

                dialogView.findViewById<TextView>(R.id.camera_tv).setOnClickListener {
                    val filename = "${System.currentTimeMillis()}.jpg"
                    val file = File(requireContext().cacheDir, filename)
                    val uri = FileProvider.getUriForFile(requireContext(), "rl.collab.diabeat.fileprovider", file)
                    takePicResult = filename to uri
                    takePicLauncher.launch(uri)
                    dialog.dismiss()
                }
            }
        }
    }
}