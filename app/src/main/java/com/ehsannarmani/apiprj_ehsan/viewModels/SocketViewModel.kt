package com.ehsannarmani.apiprj_ehsan.viewModels

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ehsannarmani.apiprj_ehsan.models.Event
import com.ehsannarmani.apiprj_ehsan.models.Favourite
import com.ehsannarmani.apiprj_ehsan.models.Lives
import com.ehsannarmani.apiprj_ehsan.models.getEventFromString
import com.ehsannarmani.apiprj_ehsan.models.toJson
import com.ehsannarmani.apiprj_ehsan.ui.screens.loadBitmap
import com.ehsannarmani.apiprj_ehsan.utils.shared
import com.google.common.io.ByteStreams
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.Socket
import java.util.concurrent.TimeUnit

class SocketViewModel : ViewModel() {

    private var mClient: Socket? = null

    private lateinit var dis: DataInputStream
    private lateinit var dos: DataOutputStream

    private val _messages = MutableStateFlow<List<Event>>(emptyList())
    val messages = _messages.asStateFlow()

    var currentPage = "home"

    private var mediaRecorder: MediaRecorder? = null

    var file by mutableStateOf("")


    var name by mutableStateOf("")
    var to by mutableStateOf("")

    var connectedToSocket by mutableStateOf(false)

    var socketIp by mutableStateOf("10.21.25.78")

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val client = OkHttpClient().newBuilder().callTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES).build()

    var isRecording by mutableStateOf(false)

    private val _stories = MutableStateFlow<Lives?>(null)
    val stories = _stories.asStateFlow()

    private val _favorites = MutableStateFlow<Favourite?>(null)
    val favourites = _favorites.asStateFlow()

    var storyLoadings = mutableStateOf(true)
    var favoritesLoadings = mutableStateOf(true)
    private val request = Request.Builder().get()
        .url("https://test-setare.s3.ir-tbz-sh1.arvanstorage.ir/wsi-lyon%2Ffavourites_avatars1.json")
        .build()

    val images = mutableStateMapOf<String, ImageBitmap>()

    var currentMusic by mutableStateOf("")
    private var player: MediaPlayer? = null

    var isPlay by mutableStateOf(false)

    init {
        getLives()
        getFavorites()
    }

    private fun getFavorites() = viewModelScope.launch(Dispatchers.IO) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = Unit

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    favoritesLoadings.value = false
                    Gson().fromJson(response.body?.string().toString(), Favourite::class.java)
                        .also { f ->
                            _favorites.update { f }
                            getImages(f.favourites.map { it.image })
                        }
                }
            }

        })
    }

    private fun getImages(urls: List<String>) = viewModelScope.launch(Dispatchers.IO) {
        urls.forEach {
            loadBitmap(it) { bitmap ->
                images[it] = bitmap.asImageBitmap()
            }
        }
    }

    private fun getLives() = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val getStories = Request.Builder().get()
                .url("https://test-setare.s3.ir-tbz-sh1.arvanstorage.ir/profile_lives2.json")
                .build()
            client.newCall(getStories).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
//                    Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        storyLoadings.value = false
                        val data = Gson().fromJson(
                            response.body?.string().toString(), Lives::class.java
                        )
                        _stories.update {
                            data
                        }
                        getImages(data.lives.map { it.profileImage })
                    }
                }
            })
        }
    }

    private lateinit var context: Context

    private val getThread = Thread {
    }

    fun playAudio(event: Event) {
        player = MediaPlayer().apply {
            setDataSource(event.content)
            start()
        }
        currentMusic = event.content
        isPlay = true
    }

    fun stopPlayer() {
        player?.stop()
        player?.release()
        player = null
        isPlay = false
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        connectedToSocket = false
        runCatching {
            this@SocketViewModel.context = context
            name = context.shared().getString("email", "") ?: ""
            mClient = Socket(socketIp, 6985)
            connectedToSocket = true
            dis = DataInputStream(mClient!!.getInputStream())
            dos = DataOutputStream(mClient!!.getOutputStream())
            getThread.start()
        }
        throwable.printStackTrace()
    }

    fun connectToSocket(context: Context) =
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            this@SocketViewModel.context = context
            name = context.shared().getString("email", "") ?: ""
            mClient = Socket(socketIp, 6985)
            connectedToSocket = true
            dis = DataInputStream(mClient!!.getInputStream())
            dos = DataOutputStream(mClient!!.getOutputStream())
            setStatus("1")

            while (true) {
                kotlin.runCatching {
                    val text = dis.readUTF()
//                    Log.d("dsfdsf", "sendMessage: $event")
                    if (text.isNullOrEmpty().not()) {
                        Log.d("dsfdsf", "sendMessage: $text")
                        kotlin.runCatching {
                            val type = JSONObject(text).getString("type")
                            if (type == USERS) {
                                val usersJson = JSONObject(text).get("content") as JSONArray
                                Log.d("dsfdsf", "connectToSocket: $usersJson $currentPage")
                                val userList = usersJson.toUsersList()
                                _users.update { userList.filter { f -> f.name != name } }
                                val user = userList.find { user -> user.name == to }
                                if (user != null) {
                                    val event = Event(
                                        name = user.name,
                                        type = ONLINE,
                                        content = user.status,
                                        to = ""
                                    )
                                    if (_messages.value.last() != event) {
                                        _messages.update { it + event }
                                    }
                                }
                            } else {
                                val event = getEventFromString(text)
                                when (type) {
                                    MESSAGE -> {
                                        Log.d("dsfdsf", "sendMessage: $event - $text")
                                        _messages.update { it + event }
                                    }

                                    IMAGE -> {
                                        _messages.update {
                                            it.toMutableList().apply {
                                                val getImage =
                                                    firstOrNull { e -> e.id == event.id }
                                                if (getImage == null) {
                                                    add(event)
                                                } else {
                                                    val index = indexOf(getImage)
                                                    removeAt(index)
                                                    val bos = ByteArrayOutputStream()
                                                    bos.write(
                                                        Base64.decode(
                                                            getImage.content, Base64.DEFAULT
                                                        )
                                                    )
                                                    bos.write(
                                                        Base64.decode(
                                                            event.content,
                                                            Base64.DEFAULT
                                                        )
                                                    )
                                                    val newContent = Base64.encodeToString(
                                                        bos.toByteArray(), Base64.DEFAULT
                                                    )
                                                    add(
                                                        index, event.copy(
                                                            content = newContent
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    VOICE -> {
                                        viewModelScope.launch(Dispatchers.IO) {
                                            val file = File(
                                                context.cacheDir,
                                                System.currentTimeMillis().toString() + ".mp3"
                                            )
                                            file.createNewFile()
                                            val fos = FileOutputStream(file)
                                            _messages.update {
                                                it.toMutableList().apply {
                                                    val getImage =
                                                        firstOrNull { e -> e.id == event.id }
                                                    if (getImage == null) {
                                                        add(event)
                                                    } else {
                                                        val index = indexOf(getImage)
                                                        removeAt(index)
                                                        fos.write(
                                                            Base64.decode(
                                                                getImage.content, Base64.DEFAULT
                                                            )
                                                        )
                                                        fos.write(
                                                            Base64.decode(
                                                                event.content, Base64.DEFAULT
                                                            )
                                                        )
                                                        add(
                                                            index, event.copy(
                                                                content = file.path
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (event.type == ONLINE) {
                                    kotlin.runCatching {
                                        _messages.update { it + event }
                                    }
                                    val foundUser =
                                        _users.value.indexOfFirst { user -> user.name == event.name }
                                    if (foundUser != -1) {
                                        _users.update {
                                            it.toMutableList().apply {
                                                removeAt(foundUser)
                                                add(foundUser, event.toUser())
                                            }.filter { f -> f.name != name }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.onFailure {
                    Log.e("dgfgfgd", "connectToSocket:${it.message} ")
                    it.printStackTrace()
                }
            }
        }

    fun setStatus(status: String) = viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
        runCatching {
            dos.writeUTF(
                Event(
                    name = name, type = ONLINE, content = status, to = ""
                ).toJson().toString()
            )
//            dos.flush()
//            mClient?.close()
        }.onFailure { it.printStackTrace() }
    }

    override fun onCleared() {
        setStatus("0")
        super.onCleared()
    }

    fun send(message: String, to: String) =
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            if (message.isNotEmpty()) {
                dos.writeUTF(
                    Event(
                        name = name, type = MESSAGE, content = message, to = to
                    ).toJson().toString()
                )
            }
        }

    fun sendFile(
        ins: InputStream, type: String = IMAGE
    ) = viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
        val event = Event(
            System.currentTimeMillis().toString(),
            name = name,
            type = type,
            content = "",
            to = to,
            step = START
        )
        runCatching {
            var lastRead = 0
            val allBytes = ByteStreams.toByteArray(ins)
            val bytes = DEFAULT_BUFFER_SIZE
            while (lastRead != allBytes.size) {
                val newBytes =
                    allBytes.copyOfRange(lastRead, (lastRead + bytes).coerceAtMost(allBytes.size))
                Log.i("TAG", "sendImage: " + newBytes.size)

                dos.writeUTF(
                    event.copy(
                        content = Base64.encodeToString(newBytes, Base64.DEFAULT)
                    ).toJson().toString()
                )
                lastRead = (lastRead + bytes).coerceAtMost(allBytes.size)
            }
            Log.d(
                "fdshfosdi", "sendImage: $lastRead  ${allBytes.size} ${lastRead == allBytes.size}"
            )
            dos.writeUTF(
                event.copy(
                    content = "", step = END
                ).toJson().toString()
            )
        }
    }

    fun startRecording(context: Context) {
        val newFile = File(context.cacheDir, System.currentTimeMillis().toString() + ",mp3")
        if (newFile.exists().not()) newFile.createNewFile()
        file = newFile.path
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
        mediaRecorder!!.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(file)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        }
        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
        isRecording = true
    }

    fun stop() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        val ins = FileInputStream(File(file))
        sendFile(ins, VOICE)
    }

    fun reconnect(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        if (mClient == null) return@launch
        if (mClient!!.isConnected.not() || mClient!!.isClosed) {
            connectToSocket(context)
            Log.d("jksgfkujdfgd", "reconnect: is not connect")
        }
    }

    companion object {
        const val MESSAGE = "message"
        const val IMAGE = "image"
        const val VOICE = "voice"
        const val ONLINE = "online"
        const val USERS = "users"
        var INSTANT: SocketViewModel? = null
        const val END = "end"
        const val START = "start"


        fun getViewModel(): SocketViewModel = synchronized(this) {
            if (INSTANT == null) {
                INSTANT = SocketViewModel()
            }
            return INSTANT!!
        }
    }
}

@Immutable
data class User(
    val name: String,
    val status: String,
)

fun JSONArray.toUsersList(): MutableList<User> {
    val list = mutableListOf<User>()
    for (i in 0..length().minus(1)) {
        val userObject = get(i) as JSONObject
        list.add(User(userObject.getString("name"), userObject.getString("status")))
    }
    return list
}

class ViewModelProvider : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (SocketViewModel.INSTANT == null) {
            SocketViewModel.INSTANT = SocketViewModel()
        }
        return SocketViewModel.INSTANT as T
    }
}