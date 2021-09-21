package com.zalesskyi.testswipe.extensions

import android.content.Context
import android.media.AudioManager
import android.os.Build

val Context.audioService
get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager