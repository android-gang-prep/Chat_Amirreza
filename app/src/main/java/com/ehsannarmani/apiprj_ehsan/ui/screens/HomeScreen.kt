package com.ehsannarmani.apiprj_ehsan.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.session.PlaybackState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.ehsannarmani.apiprj_ehsan.AppData
import com.ehsannarmani.apiprj_ehsan.R
import com.ehsannarmani.apiprj_ehsan.navigation.Routes
import com.ehsannarmani.apiprj_ehsan.ui.theme.LocalCustomColors
import com.ehsannarmani.apiprj_ehsan.utils.shared
import com.ehsannarmani.apiprj_ehsan.viewModels.LocalThemeViewModel
import com.ehsannarmani.apiprj_ehsan.viewModels.SocketViewModel
import com.ehsannarmani.apiprj_ehsan.viewModels.ViewModelProvider
import kotlinx.coroutines.coroutineScope
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavHostController, isOnline: Boolean) {


    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val viewModel: SocketViewModel = viewModel(factory = ViewModelProvider())

    val connectedToSocket = viewModel.connectedToSocket

    val users by viewModel.users.collectAsState()

    val themeViewModel = LocalThemeViewModel.current

    val darkMode by themeViewModel.darkMode.collectAsState()

    val loading by viewModel.favoritesLoadings
    val storyLoadings by viewModel.storyLoadings
    val images = viewModel.images
    val favourites by viewModel.favourites.collectAsState()
    val stories by viewModel.stories.collectAsState()

    val userLabel = remember {
        val shared = context.shared()
        shared.getString("name", null) ?: shared.getString("email", "Unknown")
    }

//    LaunchedEffect(Unit) {
//        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
//
//        sensorManager.registerListener(object : SensorEventListener {
//            override fun onSensorChanged(event: SensorEvent?) {
//                println(event?.values?.toList())
//
//                if (event?.values?.firstOrNull() != null) {
//                    val light = event.values.first()
//                    if (light >= 500) {
//                        if (darkMode) {
//                            themeViewModel.setDarkMode(false)
//                        }
//                        println("dark mode changed to false")
//                    } else {
//                        println("dark mode changed to true")
//                        if (!darkMode) {
//                            themeViewModel.setDarkMode(true)
//                        }
//                    }
//                }
//            }
//
//            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//
//            }
//
//        }, sensor, SensorManager.SENSOR_DELAY_NORMAL)
//    }
//    val lifecycle = LocalLifecycleOwner.current
//    DisposableEffect(key1 = Unit) {
//        val observer = LifecycleEventObserver() { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_RESUME -> viewModel.setStatus("1")
//                Lifecycle.Event.ON_PAUSE -> viewModel.setStatus("0")
//                else -> Unit
//            }
//        }
//        lifecycle.lifecycle.addObserver(observer)
//        onDispose {
//            lifecycle.lifecycle.removeObserver(observer)
//        }
//    }
    LaunchedEffect(key1 = isOnline) {
        viewModel.setStatus(
            if (isOnline) "1" else "0"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = userLabel.orEmpty(), color = LocalCustomColors.current.textColor)
            }
            Spacer(modifier = Modifier.height(22.dp))
            //******** Story Section ******** //
            if (storyLoadings) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val storySize = 60.dp
                stories?.lives?.forEachIndexed { index, story ->
                    Column {
                        if (index % 2 == 0) {
                            Spacer(modifier = Modifier.height((storySize.value / (2.5)).dp))
                        }
                        val image = images[story.profileImage]
                        AnimatedContent(image != null, label = "") { shouldShow ->
                            if (shouldShow) {
                                Box(modifier = Modifier
                                    .size(storySize)
                                    .clip(CircleShape)
                                    .border(
                                        3.dp,
                                        if (story.liveStreamUrl.isEmpty()) Color.Gray else Color(
                                            0xFF9C27B0
                                        ),
                                        CircleShape
                                    )
                                    .clickable {
                                        if (story.liveStreamUrl.isNotEmpty()) {
                                            AppData.streamUrl = story.liveStreamUrl
                                            navController.navigate(Routes.Stream.route)
                                        }
                                    }) {
                                    image?.let {
                                        Image(
                                            bitmap = it,
                                            contentDescription = null,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.size(storySize),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(25.dp))
                                }
                            }
                        }

                    }
                }
            }
            //******** Story Section ******** //
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .weight(1f)
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(LocalCustomColors.current.darkBackground)
                ) {
                    TextField(value = "", onValueChange = {}, modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), colors = TextFieldDefaults.colors(
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ), placeholder = {
                        Text(text = "Search...", fontSize = 13.sp)
                    })
                    Box(
                        modifier = Modifier
                            .size(55.dp)
                            .clip(
                                RoundedCornerShape(8.dp)
                            )
                            .background(LocalCustomColors.current.lightBackground)
                            .clickable {

                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                        .background(Color(0xff03A9F1))
                        .clickable {

                        }, contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Favourites:",
                color = LocalCustomColors.current.textColor,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val pagerState = rememberPagerState {
                    favourites?.favourites?.count() ?: 0
                }
                if (favourites != null) {
                    HorizontalPager(
                        state = pagerState,
                        pageSize = PageSize.Fixed(115.dp),
                        pageSpacing = 12.dp
                    ) {
                        val item = favourites!!.favourites[it]
                        Box(
                            modifier = Modifier
                                .width(115.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(40.dp))

                        ) {
                            val image = images[item.image]
                            image?.let {
                                Image(
                                    bitmap = it,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 14.dp, vertical = 22.dp)
                            ) {
                                Text(text = item.name, color = Color.White, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.heart),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(favourites?.favourites?.count() ?: 0) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (it == pagerState.currentPage) LocalCustomColors.current.active else LocalCustomColors.current.notActive
                                )
                        ) {

                        }
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
                AnimatedVisibility(visible = connectedToSocket.not()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        OutlinedTextField(value = viewModel.socketIp, onValueChange = {
                            viewModel.socketIp = it
                        }, modifier = Modifier.weight(1f), placeholder = {
                            Text(text = "Ip")
                        })
                        Button(onClick = { viewModel.connectToSocket(context) }) {
                            Text(text = "Connect")
                        }
                    }
                }
                LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                    items(users) {
                        ListItem(
                            headlineContent = {
                                Text(text = it.name)
                            },
                            colors = ListItemDefaults.colors(Color.Transparent),
                            supportingContent = {
                                if (it.status == "1") {
                                    Text(text = "Online")
                                } else {
                                    Text(text = "Offline")
                                }
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.profile),
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            modifier = Modifier.clickable { navController.navigate(Routes.Chat.route + "?to=${it.name}") })
                    }
                }
            }
        }
    }
}


suspend fun loadBitmap(
    url: String,
    onLoad: (Bitmap) -> Unit,
) = coroutineScope {
    try {
        val client = OkHttpClient().newBuilder()
            .callTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES).build()
        val request = Request.Builder().get().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val bytes = response.body!!.bytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                onLoad(bitmap)
            }

        })

    } catch (e: Exception) {
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun StreamScreen(url: String = AppData.streamUrl, navController: NavHostController) {
    val context = LocalContext.current
    val loading = remember {
        mutableStateOf(true)
    }
    val player = remember {
        ExoPlayer.Builder(context)
            .build().also {
                it.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        if (playbackState == PlaybackState.STATE_PLAYING) {
                            loading.value = false
                        }
                    }
                })
            }
    }
    DisposableEffect(Unit) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val hlsMediaSource =
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url))
        player.also {
            it.setMediaSource(hlsMediaSource)
            it.prepare()
            it.play()
        }

        onDispose {
            player.release()
        }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.background)
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { PlayerView(context).apply { this.player = player } }) {
            it.useController = false
            it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            it.hideController()
        }
        if (loading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 82.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(Color(0xFFF44336))
                    .clickable {
                        navController.popBackStack()
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }
    }

}