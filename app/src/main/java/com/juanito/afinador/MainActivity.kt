package com.juanito.afinador

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val state = TunerState()
    private val engine = PitchEngine { hz -> runOnUiThread { state.onPitch(hz) } }
    private val micGranted = mutableStateOf(false)

    private val askMic = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted.value = granted
        if (granted) engine.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            TunerScreen(
                state = state,
                micGranted = micGranted.value,
                onAskMic = { askMic.launch(Manifest.permission.RECORD_AUDIO) },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        micGranted.value = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (micGranted.value) engine.start() else askMic.launch(Manifest.permission.RECORD_AUDIO)
    }

    override fun onPause() {
        engine.stop()
        super.onPause()
    }
}
