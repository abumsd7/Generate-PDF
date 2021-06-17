package com.finalproj.generatepdf

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        when {
            intent?.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                super.onCreate(savedInstanceState)
                handleSendMultipleImages(intent) // Handle multiple images being sent
                setContentView(R.layout.activity_success)
            }
            else -> {
                // Handle other intents, such as being started from the home screen
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_main)
            }
        }

    }
    private fun handleSendMultipleImages(intent: Intent) {
        var uriList: ArrayList<Uri> = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        // Update UI to reflect multiple images being shared
        setupPermissions()
        val document = Document()
        val directoryPath = Environment.getExternalStorageDirectory().toString()
        val date = getCurrentDateTime()
        val dateInString = date.toString("yyyy_MM_dd_HH_mm_ss")
        PdfWriter.getInstance(document, FileOutputStream("$directoryPath/IndexedPDFs/$dateInString.pdf")) //  Change pdf's name.
        document.open()
        for (uris in uriList) {
            val image: Image = Image.getInstance(getRealPathFromURI(this, uris)) // Change image's name and extension.
            val scaler: Float = (document.pageSize.width - document.leftMargin()
                    - document.rightMargin() - 0) / image.width * 100 // 0 means you have no indentation. If you have any, change it.

            image.scalePercent(scaler)
            image.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP

            document.add(image)
        }
        document.close()
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Grant", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            101 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("Deny", "Permission has been denied by user")
                } else {
                    Log.i("Grant", "Permission has been granted by user")
                }
            }
        }
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val columnIndex: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor!!.moveToFirst()
            cursor!!.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }
}