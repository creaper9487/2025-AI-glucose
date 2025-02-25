package rl.collab.diabeat.frag

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import rl.collab.diabeat.Client
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.DialogSrcBinding
import rl.collab.diabeat.databinding.FragRecordBinding
import rl.collab.diabeat.fmt
import rl.collab.diabeat.str
import rl.collab.diabeat.toast
import rl.collab.diabeat.viewDialog
import java.io.File

class RecordFrag : Fragment() {
    lateinit var binding: FragRecordBinding
    private lateinit var takePicFilename: String
    private lateinit var takePicUri: Uri
    private lateinit var contentResolver: ContentResolver

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

            toast("上傳 ${fileSize.fmt()}")

            val byteArr = contentResolver.openInputStream(uri)!!.readBytes()
            val obj = byteArr.toRequestBody("image/*".toMediaType())
            val image = MultipartBody.Part.createFormData("image", filename, obj)
            Client.predictCarbohydrate(this, image)
        }
    }
    private val takePicLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val byteArr = contentResolver.openInputStream(takePicUri)!!.readBytes()
            val obj = byteArr.toRequestBody("image/jpeg".toMediaType())
            val image = MultipartBody.Part.createFormData("image", takePicFilename, obj)
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

        binding.run {
            saveBtn.setOnClickListener {
                if (AccFrag.tokens == null)
                    toast("請先登入")
                else if (glucoseEt.str.isEmpty())
                    toast("血糖值不能為空")
                else {
                    val obj = Request.Record(
                        glucoseEt.str.toDouble(),
                        carbohydrateEt.str.toDoubleOrNull(),
                        exerciseEt.str.toDoubleOrNull(),
                        insulinEt.str.toDoubleOrNull()
                    )
                    Client.postRecord(this@RecordFrag, obj)
                }
            }

            predictCarbohydrateBtn.setOnClickListener {
                if (AccFrag.tokens == null)
                    toast("請先登入")
                else {
                    val binding = DialogSrcBinding.inflate(layoutInflater)
                    val dialog = viewDialog("選擇來源", binding.root)
                    binding.photoTv.setOnClickListener {
                        pickImageLauncher.launch("image/*")
                        dialog.dismiss()
                    }
                    binding.cameraTv.setOnClickListener {
                        takePicFilename = "${System.currentTimeMillis()}.jpg"
                        val file = File(requireContext().cacheDir, takePicFilename)
                        takePicUri = FileProvider.getUriForFile(requireContext(), "rl.collab.diabeat.fileprovider", file)
                        takePicLauncher.launch(takePicUri)
                        dialog.dismiss()
                    }
                }
            }
        }
    }
}