package csc436.aitranslator

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var captureButton: Button
    private lateinit var translateButton: Button

    private lateinit var tessBaseAPI: TessBaseAPI
    private val tessDataPath by lazy { filesDir.absolutePath + "/tesseract/" }

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        supportActionBar?.hide()

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        captureButton = findViewById(R.id.captureButton)
        translateButton = findViewById(R.id.translateButton)

        // Request camera permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        // Initialize Tesseract
        initializeTesseract()

        // Capture Image
        captureButton.setOnClickListener {
            openCamera()
        }

        // Translate Text & Return to MainActivity
        translateButton.setOnClickListener {
            val extractedText = textView.text.toString()
            if (extractedText.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("extracted_text", extractedText)
                setResult(Activity.RESULT_OK, resultIntent)
                finish() // Close CameraActivity
            } else {
                Toast.makeText(this, "No text detected!", Toast.LENGTH_SHORT).show()
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
            if (success) {
                photoUri?.let { uri ->
                    imageView.setImageURI(uri)
                    val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    }

                    if (bitmap != null) {
                        processImageWithTesseract(bitmap)
                    } else {
                        Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                    }
                } ?: Toast.makeText(this, "Error: Image URI is null", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

    private fun initializeTesseract() {
        tessBaseAPI = TessBaseAPI()
        val trainedDataPath = "$tessDataPath/tessdata/"
        val trainedDataFile = File(trainedDataPath, "eng.traineddata")

        if (!trainedDataFile.exists()) {
            Log.e("TesseractOCR", "eng.traineddata NOT FOUND!")
            copyTessData(trainedDataPath)
        }

        tessBaseAPI.init(tessDataPath, "eng")
    }

    private fun processImageWithTesseract(bitmap: Bitmap) {
        tessBaseAPI.setImage(bitmap)
        val extractedText = tessBaseAPI.utF8Text
        textView.text = extractedText
        Log.d("TesseractOCR", "Extracted Text: $extractedText")
    }

    private fun copyTessData(destPath: String) {
        val assetManager = assets
        val inputStream = assetManager.open("tessdata/eng.traineddata")
        val tessDir = File(destPath)
        if (!tessDir.exists()) tessDir.mkdirs()
        val outputFile = File(destPath, "eng.traineddata")
        val outputStream = outputFile.outputStream()

        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
    }
}
