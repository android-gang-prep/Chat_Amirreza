package com.ehsannarmani.apiprj_ehsan.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ehsannarmani.apiprj_ehsan.R
import com.ehsannarmani.apiprj_ehsan.models.Event
import com.ehsannarmani.apiprj_ehsan.viewModels.SocketViewModel
import com.ehsannarmani.apiprj_ehsan.viewModels.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@Composable
fun ChatScreen(
    to: String
) {
    val viewModel: SocketViewModel = remember {
        SocketViewModel.getViewModel()
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.to = to
    }
    val name = viewModel.name
    var messages by remember {
        mutableStateOf<List<Event>>(emptyList())
    }
//    val messages by remember {
//        derivedStateOf {
//            allMessages.filter { event -> (event.to == name && event.name == to) || (event.name == name && event.to == to) }
//        }
//    }
    LaunchedEffect(key1 = name, to) {
        viewModel.messages.collectLatest { event ->
            Log.d("DFgfgdfgdf", "ChatScreen: $event")
            messages =
                event.filter { (it.name == to && it.to == name) || (it.name == name && it.to == to) }
        }
    }
    var message by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val imageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
            runCatching {
                it.let { uri ->
                    context.contentResolver.openInputStream(uri!!)
                        ?.let { it1 -> viewModel.sendFile(it1) }
                }
            }
        }
    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = viewModel.name)
                Text(text = to, modifier = Modifier.padding(top = 8.dp))
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp)
        ) {
            items(messages) {
                if (it.type == SocketViewModel.MESSAGE || it.type == SocketViewModel.IMAGE || it.type == SocketViewModel.VOICE) {
                    MessageBox(event = it, viewModel.currentMusic, viewModel.isPlay, onPlay = {
                        if (viewModel.isPlay) {
                            viewModel.stopPlayer()
                        } else {
                            viewModel.playAudio(it)
                        }
                    }, it.name == viewModel.name)
                } else {
                    OnlineBox(to, it.content == "1")
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            1.dp
                        ),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(text = "Message...")
                    }
                )
                Box(
                    modifier = Modifier
                        .height(TextFieldDefaults.MinHeight)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray)
                            .size(48.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    if (viewModel.isRecording) {
                                        viewModel.stop()
                                    } else {
//                                        viewModel.startRecording(context)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_mic_24),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00AC83))
                        .size(48.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                imageLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                            contentDescription = null
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF837DFF))
                        .size(48.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                viewModel.send(message, to)
                                message = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Rounded.Send, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun OnlineBox(name: String, online: Boolean) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = "$name is ${if (online) "online" else "offline"}",
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(0.6f))
                .padding(12.dp)
        )
    }
}


@Composable
fun MessageBox(
    event: Event,
    currentPlayed: String,
    isPlay: Boolean,
    onPlay: () -> Unit,
    isMyMessage: Boolean
) {
    val align = if (isMyMessage) {
        Alignment.CenterEnd
    } else Alignment.CenterStart
    val color = if (isMyMessage) {
        MaterialTheme.colorScheme.tertiary
    } else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val shape = RoundedCornerShape(20.dp)
    Box(modifier = Modifier.fillMaxWidth(), align) {
        Surface(
            shape = shape,
            color = color
        ) {
            if (event.type == SocketViewModel.MESSAGE) {
                Text(text = event.content, Modifier.padding(12.dp))
            } else {
                if (event.type == SocketViewModel.IMAGE) {
                    var image by remember {
                        mutableStateOf<ImageBitmap?>(null)
                    }
                    LaunchedEffect(key1 = event) {
                        withContext(Dispatchers.IO) {
                            runCatching {
                                val byteArray = Base64.decode(event.content, Base64.DEFAULT)
                                image = BitmapFactory.decodeByteArray(
                                    byteArray, 0, byteArray.size
                                ).asImageBitmap()
                            }.onFailure {
                                it.printStackTrace()
                                Log.d(
                                    "sdfdfsd",
                                    "MessageBox: ${it.message} ${event.content.length}"
                                )
                            }
                        }
                    }
                    if (image != null) {
                        Image(
                            bitmap = image!!,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .heightIn(min = 100.dp)
                                .widthIn(100.dp, 200.dp)
                        )
                    }
                } else {
                    IconButton(onClick = {
                        onPlay.invoke()
                    }) {
                        if (isPlay) {
                            Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.round_pause_24),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}
