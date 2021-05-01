package org.tjur.simplestatemachine

import java.lang.IllegalStateException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

open class SimpleStateMachine(private val initialState: KClass<*>) : Runnable {

    private val messageQueue: BlockingQueue<Message> = LinkedBlockingQueue()
    private val states: MutableMap <String, State> = HashMap()
    private lateinit var currentState: State

    override fun run() {
        currentState = getState(initialState)
        currentState.enter().transition?.let {
            if (it.clearQueue) messageQueue.clear()
            messageQueue.put(it)
        }
        while (true) {
            when (val message = messageQueue.take()) {
                is TransitionMessage -> {
                    currentState.leave()
                    currentState = getState(message.state)
                    val result = currentState.enter(message.message)
                    if (result.transition == null) continue
                    if (result.transition.clearQueue) messageQueue.clear()
                    messageQueue.put(result.transition)
                }
                is StopMessage -> {
                    break
                }
                else -> {
                    val result = process(message, currentState)
                    if (result.transition == null) continue
                    if (result.transition.clearQueue) messageQueue.clear()
                    messageQueue.put(result.transition)
                }
            }
        }
        currentState.leave()
    }

    fun prepareState(state: State) {
        val name = state::class.qualifiedName ?: throw Exception("Could not get qualified name of $state")
        if (states.containsKey(name)) {
            throw Exception("$name has already been added.")
        }
        states[name] = state
    }

    /**
     * Send a Message to be processed by the current state in the state machine (or by any of its parents)
     *
     * @param message: Message to be processed
     */
    fun process(message: Message) {
        messageQueue.put(message)
    }

    /**
     * Stops the state machine, but allowing it to finish what it's currently doing.
     */
    fun stop() {
        messageQueue.clear()
        messageQueue.put(StopMessage())
    }

    private fun getState(kClass: KClass<*>): State {
        val stateName = kClass.qualifiedName ?: throw Exception("Could not retrieve the qualified name of $kClass.")
        val state = states[stateName]
        if (state != null) {
            return state
        }
        val newState = kClass.createInstance()
        if (newState !is State) {
            throw IllegalStateException("$stateName does not extend State.")
        }
        states[stateName] = newState
        return newState
    }

    private fun process(message: Message, state: State): MessageResult {
        var messageResult = state.process(message)
        if (!messageResult.handled) {
            val parent: KClass<*>? = state.getParentState()
            if (parent != null) {
                val parentState = getState(parent)
                messageResult = process(message, parentState)
            }
        }
        return messageResult
    }
}