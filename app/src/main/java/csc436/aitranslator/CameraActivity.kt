package csc436.aitranslator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*

import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var captureButton: Button
    private lateinit var translateButton: Button
    private lateinit var languageButton: Button
    private lateinit var closeButton: ImageButton
    private lateinit var deleteImageButton: ImageButton

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var photoUri: Uri? = null
    private var selectedLanguageCode: String = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        supportActionBar?.hide()

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        captureButton = findViewById(R.id.captureButton)
        translateButton = findViewById(R.id.translateButton)
        languageButton = findViewById(R.id.languageButton)
        closeButton = findViewById(R.id.closeButton)
        deleteImageButton = findViewById(R.id.deleteImageButton)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        closeButton.setOnClickListener { finish() }
        captureButton.setOnClickListener { openCamera() }
        languageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            languagePickerLauncher.launch(intent)
        }
        translateButton.setOnClickListener { translateText() }
        deleteImageButton.setOnClickListener { removeImage() }
    }

    private fun startTranslatingAnimation() {
        lifecycleScope.launch {
            var dotCount = 0
            while (true) {
                val dots = ".".repeat(dotCount % 4)
                translateButton.text = getString(R.string.translating) + dots
                dotCount++
                delay(500)
                if (translateButton.text == getString(R.string.translate)) break
            }
        }
    }

    private fun openCamera() {
        val photoFile = File(getExternalFilesDir(null), "captured_image.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
        photoUri = uri
        cameraLauncher.launch(uri)
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                imageView.setImageURI(null)
                imageView.setImageURI(photoUri)
                deleteImageButton.visibility = View.VISIBLE

                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, photoUri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    contentResolver.openInputStream(photoUri!!)?.use { BitmapFactory.decodeStream(it) }
                }

                if (bitmap != null) {
                    extractTextFromImage(bitmap)
                } else {
                    showToast(getString(R.string.failed_to_process))
                }
            } else {
                showToast(getString(R.string.failed_to_capture))
            }
        }

    private fun removeImage() {
        imageView.setImageDrawable(null)
        deleteImageButton.visibility = View.GONE
        photoUri = null
    }

    private fun extractTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                textView.text = visionText.text.ifEmpty { getString(R.string.no_text_found) }
                Log.d("MLKitOCR", "Extracted Text: ${visionText.text}")
            }
            .addOnFailureListener { e ->
                Log.e("MLKitOCR", "Failed to extract text: ${e.message}")
                showToast(getString(R.string.failed_to_extract_text, e.message))
            }
    }

    private val languagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                selectedLanguageCode = data?.getStringExtra("selectedCode") ?: "en"
                val selectedLanguage = data?.getStringExtra("selectedLanguage") ?: "English"
                languageButton.text = selectedLanguage
            }
        }

    private fun translateText() {
        val extractedText = textView.text.toString().trim()

        if (photoUri == null) {
            Log.e("Translation", getString(R.string.no_image_found))
            showToast(getString(R.string.no_image_found))
            return
        }

        if (extractedText.isEmpty() || extractedText == getString(R.string.no_text_found)) {
            Log.e("Translation", getString(R.string.no_text_found))
            showToast(getString(R.string.no_text_found))
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            startTranslatingAnimation()

            val translatedText = withContext(Dispatchers.IO) {
                OpenAIRepository().translateText(extractedText, selectedLanguageCode)
            }

            textView.text = translatedText
            translateButton.text = getString(R.string.translate)
        }
    }



    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(App.applyLanguageToContext(newBase))
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
