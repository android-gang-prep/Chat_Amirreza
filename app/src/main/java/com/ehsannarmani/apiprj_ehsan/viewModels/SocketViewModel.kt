package com.ehsannarmani.apiprj_ehsan.viewModels

import android.content.Context
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
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.util.concurrent.TimeUnit

class SocketViewModel : ViewModel() {

    private var mClient: Socket? = null

    private lateinit var dis: DataInputStream
    private lateinit var dos: DataOutputStream

    private val _messages = MutableStateFlow<List<Event>>(emptyList())
    val messages = _messages.asStateFlow()

    var currentPage = "home"


    var name by mutableStateOf("")
    var to by mutableStateOf("")

    var connectedToSocket by mutableStateOf(false)

    var socketIp by mutableStateOf("10.21.25.78")

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val client = OkHttpClient()
        .newBuilder()
        .callTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()

    private val _stories = MutableStateFlow<Lives?>(null)
    val stories = _stories.asStateFlow()

    private val _favorites = MutableStateFlow<Favourite?>(null)
    val favourites = _favorites.asStateFlow()

    var storyLoadings = mutableStateOf(true)
    var favoritesLoadings = mutableStateOf(true)
    private val request = Request
        .Builder()
        .get()
        .url("https://test-setare.s3.ir-tbz-sh1.arvanstorage.ir/wsi-lyon%2Ffavourites_avatars1.json")
        .build()

    val images = mutableStateMapOf<String, ImageBitmap>()

    init {
        getLives()
        getFavorites()
    }

    private fun getFavorites() = viewModelScope.launch(Dispatchers.IO) {
        client.newCall(request)
            .enqueue(object : Callback {
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
            val getStories = Request
                .Builder()
                .get()
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
                            response.body?.string().toString(),
                            Lives::class.java
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

    private val getThread =Thread {
        while (true) {
            val text = dis.readUTF()
            if (text.isNullOrEmpty().not()) {
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
                                user.name,
                                type = ONLINE,
                                content = user.status,
                                to
                            )
                            if (_messages.value.last() != event) {
                                _messages.update { it + event }
                            }
                        }
                    } else {
                        val event = getEventFromString(text)
                        Log.d("dsfdsf", "sendMessage: $event $currentPage")
                        if ((event.to == name && event.name == to) || (event.name == name && event.to == to)) {
                            _messages.update { it + event }
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
        }
    }

    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }

    fun connectToSocket(context: Context) =
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            name = context.shared().getString("email", "") ?: ""
            mClient = Socket(socketIp, 6985)
            connectedToSocket = true
            dis = DataInputStream(mClient!!.getInputStream())
            dos = DataOutputStream(mClient!!.getOutputStream())
            setStatus("1")
            getThread.start()
        }

    fun setStatus(status: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            dos.writeUTF(
                Event(
                    name = name,
                    type = ONLINE,
                    content = status,
                    to = ""
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

    fun send(message: String, to: String) = viewModelScope.launch(Dispatchers.IO) {
        if (message.isNotEmpty()){
            dos.writeUTF(
                Event(
                    name,
                    type = MESSAGE,
                    content = message,
                    to = to
                ).toJson().toString()
            )
        }
    }

    companion object {
        const val MESSAGE = "message"
        const val ONLINE = "online"
        const val USERS = "users"
        var INSTANT: SocketViewModel? = null
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