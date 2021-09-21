package com.zalesskyi.testswipe.utils

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.zalesskyi.testswipe.extensions.audioService

@Composable
fun rememberRecognizer(context: Context): SpeechRecognizer {
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }
    val volumeIndex = remember {
        context.audioService.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
    }
    DisposableEffect(context) {
        val speechListener =
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.i("SpeechListener", "onReadyForSpeech")
                }

                override fun onBeginningOfSpeech() {
                    Log.i("SpeechListener", "onBeginningOfSpeech")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    Log.i("SpeechListener", "onRmsChanged")
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    Log.i("SpeechListener", "onBufferReceived")
                }

                override fun onEndOfSpeech() {
                    Log.i("SpeechListener", "onEndOfSpeech")
                }

                override fun onError(error: Int) {
                    Log.i("SpeechListener", "onError")
                    context.audioService.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volumeIndex, 0)
                }

                override fun onResults(results: Bundle?) {
                    val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    context.audioService.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volumeIndex, 0)
                    Log.i("SpeechListener", "onResults: ${result?.first()}")
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val result = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.i("SpeechListener", "onPartialResults: ${result?.first()}")
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    Log.i("SpeechListener", "onEvent")
                }
            }

        speechRecognizer.apply {
            setRecognitionListener(speechListener)
        }

        onDispose {
            speechRecognizer.stopListening()
            speechRecognizer.destroy()
        }
    }
    return speechRecognizer
}

@Composable
fun rememberSpeechRecognitionIntent(context: Context): Intent {
    return remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
    }
}