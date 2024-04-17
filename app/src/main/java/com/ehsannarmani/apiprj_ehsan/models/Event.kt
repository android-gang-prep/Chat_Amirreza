package com.ehsannarmani.apiprj_ehsan.models

import androidx.compose.runtime.Immutable
import com.ehsannarmani.apiprj_ehsan.viewModels.User
import org.json.JSONObject

@Immutable
data class Event(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val type: String,
    val content: String,
    val to: String,
    val step :String= ""
) {
    fun toUser() = User(name, content)
}


fun getEventFromString(data: String): Event {
    val json = JSONObject(data)
    return Event(
        json.getString("id"),
        json.getString("name"),
        json.getString("type"),
        json.getString("content"),
        json.getString("to"),
        json.getCrashString("step"),
    )
}


fun Event.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("name", name)
        put("content", content)
        put("type", type)
        put("to", to)
        put("step", step)
    }
}

fun JSONObject.getCrashString(key:String): String{
    return try {
        getString(key)
    }catch (_:Exception){
        ""
    }
}