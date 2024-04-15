package com.ehsannarmani.apiprj_ehsan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ehsannarmani.apiprj_ehsan.R
import com.ehsannarmani.apiprj_ehsan.models.Event
import com.ehsannarmani.apiprj_ehsan.viewModels.SocketViewModel
import com.ehsannarmani.apiprj_ehsan.viewModels.ViewModelProvider
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChatScreen(
    to: String
) {
    val viewModel: SocketViewModel = viewModel(factory = ViewModelProvider())
    val messages by viewModel.messages.collectAsState(emptyList())
    var message by remember {
        mutableStateOf("")
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.to = to
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp)
        ) {
            items(messages) {
                if (it.type == SocketViewModel.MESSAGE) {
                    MessageBox(event = it, it.name == viewModel.name)
                } else {
                    OnlineBox(to, it.content == "1")
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(text = "Message...")
                }
            )
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00AC83))
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.Home, contentDescription = null)
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
    Text(
        text = "$name is ${if (online) "online" else "offline"}",
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(0.6f))
            .padding(12.dp)
    )
}

@Composable
fun MessageBox(event: Event, isMyMessage: Boolean) {
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
            Text(text = event.content, Modifier.padding(12.dp))
        }
    }
}
