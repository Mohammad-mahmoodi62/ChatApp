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
@SerialName("chat-message")
class ChatMessage(val message: String, val senderId: String, val receiverId: String) : BaseMessage()

@Serializable
class UserInfo(val ID: String, val Name: String, var IP: String) : BaseMessage()


@Serializable
@SerialName("greetings-server")
class HelloServer(val user: UserInfo) : BaseMessage()

@Serializable
@SerialName("greetings-client")
class HelloClient(val user: UserInfo) : BaseMessage()