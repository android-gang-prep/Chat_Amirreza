package com.ehsannarmani.apiprj_ehsan.models

import androidx.compose.runtime.Immutable
import com.ehsannarmani.apiprj_ehsan.viewModels.User
import org.json.JSONObject

@Immutable
data class Event(
    val name: String,
    val type: String,
    val content: String,
    val to: String
) {
    fun toUser() = User(name, content)
}


fun getEventFromString(data: String): Event {
    val json = JSONObject(data)
    return Event(
        json.getString("name"),
        json.getString("type"),
        json.getString("content"),
        json.getString("to")
    )
}


fun Event.toJson(): JSONObject {
    return JSONObject().apply {
        put("name", name)
        put("content", content)
        put("type", type)
        put("to", to)
    }
}