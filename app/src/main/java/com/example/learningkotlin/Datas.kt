package com.example.learningkotlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BaseMessage{
    var status = "open"
}

@Serializable
@SerialName("sam-service")
class OwnedProject2(var msg: String, var owner: String) : BaseMessage()

@Serializable
@SerialName("identify-your-self-msg")
class revealMessage() : BaseMessage()


@Serializable
@SerialName("identification-msg")
class hereIAm() : BaseMessage()


@Serializable
@SerialName("request-number")
class requesterNumber(var num: Int) : BaseMessage()


@Serializable
@SerialName("response-number")
class responserNumber(var num: Int) : BaseMessage()

@Serializable
@SerialName("server-ready")
class ServerIsReady() : BaseMessage()
