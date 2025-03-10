package csc436.aitranslator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var captureButton: Button
    private lateinit var translateButton: Button
    private lateinit var languageButton: Button
    private lateinit var closeButton: ImageButton

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build()) // ✅ Multilingual OCR
    private var photoUri: Uri? = null
    private var selectedLanguageCode: String = "en" // Default to English

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

        // Request Camera Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        closeButton.setOnClickListener {
            finish() // Close CameraActivity & go back
        }

        // Capture Image
        captureButton.setOnClickListener {
            openCamera()
        }

        // Select Language
        languageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            languagePickerLauncher.launch(intent)
        }

        // Translate Extracted Text
        translateButton.setOnClickListener {
            val extractedText = textView.text.toString().trim()
            if (extractedText.isNotEmpty()) {
                translateText(extractedText)
            } else {
                Toast.makeText(this, "No text to translate!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Open Camera and Save Image
    private fun openCamera() {
        val photoFile = File(getExternalFilesDir(null), "captured_image.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)

        photoUri = uri
        cameraLauncher.launch(uri)
    }

    // Handle Image Capture Result
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                imageView.setImageURI(null)
                imageView.setImageURI(photoUri)

                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, photoUri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    contentResolver.openInputStream(photoUri!!)?.use { BitmapFactory.decodeStream(it) }
                }

                if (bitmap != null) {
                    extractTextFromImage(bitmap)
                } else {
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }


    // Extract Text from Image using ML Kit
    private fun extractTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                textView.text = if (extractedText.isNotEmpty()) extractedText else "No text found."
                Log.d("MLKitOCR", "Extracted Text: $extractedText")
            }
            .addOnFailureListener { e ->
                Log.e("MLKitOCR", "Failed to extract text: ${e.message}")
                Toast.makeText(this, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Handle Language Selection Result
    private val languagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                selectedLanguageCode = data?.getStringExtra("selectedCode") ?: "en"
                val selectedLanguage = data?.getStringExtra("selectedLanguage") ?: "English"
                languageButton.text = selectedLanguage
            }
        }

    // Translate Extracted Text using OpenAI API
    private fun translateText(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val translatedText = OpenAIRepository().translateText(text, selectedLanguageCode)

            runOnUiThread {
                textView.text = translatedText
                Toast.makeText(this@CameraActivity, "Translation Complete", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
