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
class RevealMessage() : BaseMessage()


@Serializable
@SerialName("identification-msg")
class HereIAm(val Info: UserInfo) : BaseMessage()


@Serializable
@SerialName("request-number")
class RequesterNumber(var num: Int) : BaseMessage()


@Serializable
@SerialName("response-number")
class ResponderNumber(var num: Int) : BaseMessage()

@Serializable
@SerialName("chat-message")
class ChatMessage(val message: String, val senderId: String, val receiverId: String) : BaseMessage()

@Serializable
class UserInfo(val ID: String, val Name: String, var IP: String, var lastMsg: String = "") : BaseMessage()


@Serializable
@SerialName("greetings-server")
class HelloServer(val user: UserInfo) : BaseMessage()

@Serializable
@SerialName("greetings-client")
class HelloClient(val user: UserInfo) : BaseMessage()

@Serializable
@SerialName("alice-public-key")
class AlicePubKey(val pubKey:ByteArray) : BaseMessage()

@Serializable
@SerialName("bob-public-key")
class BobPubKey(val pubKey:ByteArray) : BaseMessage()