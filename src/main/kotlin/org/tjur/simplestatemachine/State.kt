package org.tjur.simplestatemachine

import kotlin.reflect.KClass

abstract class State {

    open fun enter(message: Message? = null) = handled()
    open fun leave() {}
    abstract fun process(message: Message): MessageResult

    open fun getParentState() : KClass<*>? = null

    fun handled() = MessageResult(true, null)

    fun unhandled() = MessageResult(false)

    fun handled(kClass: KClass<*>): MessageResult {
        return MessageResult(true, TransitionMessage(kClass, true, null))
    }

    fun handled(kClass: KClass<*>, clearQueue: Boolean, message: Message?): MessageResult {
        return MessageResult(true, TransitionMessage(kClass, clearQueue, message))
    }
}