package rl.collab.diabeat.onclick

import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import rl.collab.diabeat.Client
import rl.collab.diabeat.databinding.DialogSrcBinding
import rl.collab.diabeat.fmt
import rl.collab.diabeat.frag.AccFrag
import rl.collab.diabeat.frag.RecordFrag
import rl.collab.diabeat.toast
import rl.collab.diabeat.viewDialog
import java.io.File

class PredictCarbohydrateBtnListener(private val recordFrag: RecordFrag) : View.OnClickListener {
    private lateinit var takePicFilename: String
    private lateinit var takePicUri: Uri

    private val pickImageLauncher = recordFrag.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val contentResolver = recordFrag.requireContext().contentResolver

            lateinit var filename: String
            var fileSize = 0L

            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                cursor.moveToFirst()
                filename = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
            }

            recordFrag.toast("上傳 ${fileSize.fmt()}")

            val byteArr = contentResolver.openInputStream(uri)!!.readBytes()
            val obj = byteArr.toRequestBody("image/*".toMediaType())
            val image = MultipartBody.Part.createFormData("image", filename, obj)
            Client.predictCarbohydrate(recordFrag, image)
        }
    }

    private val takePicLauncher = recordFrag.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val contentResolver = recordFrag.requireContext().contentResolver

            val byteArr = contentResolver.openInputStream(takePicUri)!!.readBytes()
            val obj = byteArr.toRequestBody("image/jpeg".toMediaType())
            val image = MultipartBody.Part.createFormData("image", takePicFilename, obj)
            Client.predictCarbohydrate(recordFrag, image)
        }
    }

    override fun onClick(p0: View?) {
        if (AccFrag.tokens == null)
            recordFrag.toast("請先登入")
        else {
            val binding = DialogSrcBinding.inflate(recordFrag.layoutInflater)
            val dialog = recordFrag.viewDialog("選擇來源", binding.root, null)

            binding.photoTv.setOnClickListener {
                pickImageLauncher.launch("image/*")
                dialog.dismiss()
            }

            binding.cameraTv.setOnClickListener {
                val context = recordFrag.requireContext()

                takePicFilename = "${System.currentTimeMillis()}.jpg"
                val file = File(context.cacheDir, takePicFilename)
                takePicUri = FileProvider.getUriForFile(context, "rl.collab.diabeat.fileprovider", file)
                takePicLauncher.launch(takePicUri)
                dialog.dismiss()
            }
        }
    }
}