package rl.collab.diabeat.frag

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import rl.collab.diabeat.Request
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.DialogSrcBinding
import rl.collab.diabeat.databinding.FragRecordBinding
import rl.collab.diabeat.str
import java.io.File

class RecordFrag : MyFrag<FragRecordBinding>() {
    private lateinit var takePicFilename: String
    private lateinit var takePicUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val masterKey = MasterKey.Builder(appCon)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        vm.remePref = EncryptedSharedPreferences.create(
            appCon,
            "reme",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun binder(): Binder<FragRecordBinding> = FragRecordBinding::inflate

    override fun FragRecordBinding.setView() {
        saveBtn.setOnClickListener {
            vm.acc ?: run {
                toast("請先登入🔑")
                return@setOnClickListener
            }

            if (glucoseEt.str.isEmpty()) {
                toast("血糖值不能為空❗")
                return@setOnClickListener
            }

            val obj = Request.Record(
                glucoseEt.str.toDouble(),
                carbohydrateEt.str.toDoubleOrNull(),
                exerciseEt.str.toDoubleOrNull(),
                insulinEt.str.toDoubleOrNull()
            )
            reqPostRecords(it, obj)
        }

        predictCarbohydrateBtn.setOnClickListener {
            vm.acc ?: run {
                toast("請先登入🔑")
                return@setOnClickListener
            }

            val binding = DialogSrcBinding.inflate(layoutInflater)
            val dialog = dialog("選擇來源", view = binding.root, pos = null, neg = null, cancelable = true)
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

    private fun reqPostRecords(btn: View, obj: Request.Record) {
        btn.isEnabled = false

        val onSucceed = { _: Response<Result.Records> ->
            toast("已儲存✅")
            binding.run {
                glucoseEt.str = ""
                carbohydrateEt.str = ""
                exerciseEt.str = ""
                insulinEt.str = ""
                etGroup.clearFocus()
            }
            btn.isEnabled = true
        }
        val onFail = { btn.isEnabled = true }
        request(onSucceed, null, onFail, false) { postRecord(vm.access!!, obj) }
    }

    private fun reqPredictCarbohydrate(btn: View, image: MultipartBody.Part) {
        btn.isEnabled = false

        val onSucceed = { r: Response<Result.Predict> ->
            binding.carbohydrateEt.str = "%.0f".format(r.body()!!.predicted_value)
            btn.isEnabled = true
        }
        val onFail = { btn.isEnabled = true }
        request(onSucceed, null, onFail, false) { predictCarbohydrate(vm.access!!, image) }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult

        var filename = ""
        var fileSize = 0L
        val resolver = requireContext().contentResolver
        val cursor = resolver.query(uri, null, null, null, null)
        cursor?.use {
            cursor.moveToFirst()
            filename = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
        }

        toast(
            if (fileSize > 1048576)
                "上傳 %.1f MB".format(fileSize / 1048576.0)
            else
                "上傳 %.1f KB".format(fileSize / 1024.0)
        )

        val byteArr = resolver.openInputStream(uri)!!.readBytes()
        val obj = byteArr.toRequestBody("image/*".toMediaType())
        val image = MultipartBody.Part.createFormData("image", filename, obj)
        reqPredictCarbohydrate(binding.predictCarbohydrateBtn, image)
    }

    private val takePicLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success)
            return@registerForActivityResult

        val resolver = requireContext().contentResolver

        val byteArr = resolver.openInputStream(takePicUri)!!.readBytes()
        val obj = byteArr.toRequestBody("image/jpeg".toMediaType())
        val image = MultipartBody.Part.createFormData("image", takePicFilename, obj)
        reqPredictCarbohydrate(binding.predictCarbohydrateBtn, image)
    }
}