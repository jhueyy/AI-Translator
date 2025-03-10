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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        deleteImageButton = findViewById(R.id.deleteImageButton)

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
        // Translate Extracted Text
        translateButton.setOnClickListener {
            translateText() // Call without parameters
        }

        // Delete Image
        deleteImageButton.setOnClickListener {
            removeImage()
        }

    }

    private fun startTranslatingAnimation() {
        lifecycleScope.launch {
            var dotCount = 0
            while (true) { // Infinite loop until translation finishes
                val dots = ".".repeat(dotCount % 4) // Cycles: "", ".", "..", "..."
                translateButton.text = "Translating$dots"
                dotCount++
                delay(500) // Change every 0.5 seconds

                // Stop when translation is complete
                if (translateButton.text == "Translate") break
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
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    private fun removeImage() {
        imageView.setImageDrawable(null) // Clear image
        deleteImageButton.visibility = View.GONE // Hide delete button
        photoUri = null // Remove reference
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
    private fun translateText() {
        val extractedText = textView.text.toString().trim()

        // Check if an image exists
        if (photoUri == null) {
            Log.e("Translation", "No image found!")
            return
        }

        // Check if text was detected
        if (extractedText.isEmpty() || extractedText == "No text found.") {
            Log.e("Translation", "No text detected!")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            startTranslatingAnimation() // Start animation

            val translatedText = withContext(Dispatchers.IO) {
                OpenAIRepository().translateText(extractedText, selectedLanguageCode)
            }

            textView.text = translatedText
            translateButton.text = "Translate" // Reset button text
        }
    }

}
