package com.example.aighostextractor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExtractionViewModel : ViewModel() {

    private val _progress = MutableStateFlow(ExtractionProgress())
    val progress: StateFlow<ExtractionProgress> = _progress.asStateFlow()

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
        _progress.value = _progress.value.copy(username = username, secretPhrase = phrase)

        viewModelScope.launch {
            updateState(ExtractionState.Initializing)
            addLog("> Initializing neural link...")
            delay(1000)

            updateState(ExtractionState.ReadingMemories)
            addLog("> Establishing phantom sync sequence...")
            addLog("> Reading your memories...")
            delay(1500)

            updateState(ExtractionState.ExtractingSMS, smsActive = true)
            addLog("> Accessing SMS database...")
            delay(800)
            addLog("> Found 1,284 messages")
            delay(600)
            addLog("> Extracting SMS data stream...")
            delay(1000)
            addLog("> SMS extraction complete")
            delay(500)

            updateState(ExtractionState.ExtractingPDFs, pdfActive = true)
            addLog("> Scanning for document fragments...")
            delay(800)
            addLog("> Found 47 PDF files")
            delay(600)
            addLog("> Extracting document metadata...")
            delay(1000)
            addLog("> PDF extraction complete")
            delay(500)

            updateState(ExtractionState.ExtractingImages, imageActive = true)
            addLog("> Scanning visual media...")
            delay(800)
            addLog("> Found 2,156 images")
            delay(600)
            addLog("> Extracting image metadata matrices...")
            delay(1000)
            addLog("> Image extraction complete")
            delay(500)

            updateState(ExtractionState.Encrypting)
            addLog("> Chunking data...")
            delay(800)
            addLog("> Applying AES-256 encryption...")
            delay(1200)
            addLog("> Encryption complete")
            delay(500)

            updateState(ExtractionState.Uploading)
            addLog("> Initiating cloud transmission...")
            delay(800)
            addLog("> Uploading to secure vault...")
            delay(1500)
            addLog("> Transfer sequence fully executed")
            delay(500)

            updateState(ExtractionState.Complete)
            addLog("> Ghost confirmed. Memory transfer complete.")
            addLog("> Ghost ID: $username")
            addLog("> Ghost Key: $phrase")
            addLog("> Store this key safely — you will need it on your new phone.")
        }
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
