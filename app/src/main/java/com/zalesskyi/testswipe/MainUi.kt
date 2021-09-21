package com.zalesskyi.testswipe

import android.Manifest
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.permissions.rememberPermissionState
import com.zalesskyi.testswipe.custom.SwipeCard
import com.zalesskyi.testswipe.extensions.audioService
import com.zalesskyi.testswipe.ui.theme.TestSwipeTheme
import com.zalesskyi.testswipe.utils.rememberRecognizer
import com.zalesskyi.testswipe.utils.rememberSpeechRecognitionIntent
import java.util.*


@ExperimentalMaterialApi
@Composable
fun MainUi(onSwiped: () -> Unit) {
    val state = remember {
        mutableStateListOf("1", "2", "3", "1", "2", "3", "1", "2", "3", "1", "2", "3")
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxSize()) {
            state.forEachIndexed { _, person ->
                /**
                 * SwipeCard is a swipeable card all together which is a BOX by nature
                 * and supports touch events of drag
                 */
                SwipeCard(
                    onSwiped = { onSwiped() }, modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopCenter)
                ) {
                    /**
                     * Actual person card to hold person details
                     */
                    CardContent()
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    TestSwipeTheme {
        CardContent()
    }
}

@Composable
fun CardContent() {
    ConstraintLayout(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        val guidelineStart = createGuidelineFromStart(16.dp)
        val guidelineEnd = createGuidelineFromEnd(16.dp)
        val guidelineTop = createGuidelineFromTop(48.dp)
        val guidelineBottom = createGuidelineFromBottom(16.dp)

        val (tPhrase, tPhonetic, bListen, tSpoken, bRecord, bSkip) = createRefs()
        Text("Six sick hicks nick six slick bricks with picks and sticks.",
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .constrainAs(tPhrase) {
                    top.linkTo(guidelineTop)
                    start.linkTo(guidelineStart)
                    end.linkTo(bListen.start)
                    width = Dimension.fillToConstraints
                })
        Text(
            "[:Six sick hicks :nick s:ix slick' bricks with :picks and st'icks.']",
            fontSize = 14.sp,
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(top = 24.dp)
                .constrainAs(tPhonetic) {
                    top.linkTo(tPhrase.bottom)
                    start.linkTo(guidelineStart)
                    end.linkTo(guidelineEnd)
                    width = Dimension.fillToConstraints
                }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_play_circle_outline_black_24dp),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp)
                .constrainAs(bListen) {
                    top.linkTo(guidelineTop)
                    end.linkTo(guidelineEnd)
                }
        )
        DrawableText(
            icon = painterResource(id = R.drawable.ic_chevron_left_black_24dp),
            text = "Skip",
            modifier = Modifier
                .constrainAs(bSkip) {
                    bottom.linkTo(guidelineBottom)
                    start.linkTo(guidelineStart)
                }
        )
        SpeechRecognitionButton(modifier = Modifier
            .padding(16.dp)
            .constrainAs(bRecord) {
                linkTo(top = parent.top, bottom = parent.bottom, bias = 0.7F)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            })
        Text(
            "Six sick hicks nick six slick bricks with picks and sticks.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .constrainAs(tSpoken) {
                    linkTo(top = tPhonetic.bottom, bottom = bRecord.top, bias = 0.75F)
                    start.linkTo(guidelineStart)
                    end.linkTo(guidelineEnd)
                    width = Dimension.fillToConstraints
                }
        )
    }
}

@Composable
fun DrawableText(
    icon: Painter,
    text: String,
    modifier: Modifier,
    style: TextStyle = MaterialTheme.typography.h6
) {
    Row(modifier = modifier) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = text, style = style)
    }
}

@Composable
fun SpeechRecognitionButton(modifier: Modifier) {
    val context = LocalContext.current
    val speechRecognizer = rememberRecognizer(context = context)
    val intent = rememberSpeechRecognitionIntent(context = context)
    val permissionState = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                speechRecognizer.startListening(intent)
            }
        }
    )

    Image(
        painter = painterResource(id = R.drawable.ic_play_circle_outline_black_24dp),
        contentDescription = null,
        modifier = modifier.clickable {
            context.audioService.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
            permissionState.launch(Manifest.permission.RECORD_AUDIO)
        }
    )
}