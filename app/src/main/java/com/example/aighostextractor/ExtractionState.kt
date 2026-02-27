package com.example.aighostextractor

sealed class ExtractionState {
    data object Idle : ExtractionState()
    data object Initializing : ExtractionState()
    data object ReadingMemories : ExtractionState()
    data object ExtractingSMS : ExtractionState()
    data object ExtractingPDFs : ExtractionState()
    data object ExtractingImages : ExtractionState()
    data object Encrypting : ExtractionState()
    data object Uploading : ExtractionState()
    data object Complete : ExtractionState()
}

data class ExtractionProgress(
    val state: ExtractionState = ExtractionState.Idle,
    val logs: List<String> = listOf(
        "> System initialized...",
        "> Awaiting sync command..."
    ),
    val smsActive: Boolean = false,
    val pdfActive: Boolean = false,
    val imageActive: Boolean = false,
    val secretPhrase: String = "",
    val username: String = ""
)
