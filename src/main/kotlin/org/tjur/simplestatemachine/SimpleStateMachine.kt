package org.tjur.simplestatemachine

import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

open class SimpleStateMachine(private val initialState: KClass<*>) : Runnable {

    private val messageQueue: BlockingQueue<Message> = LinkedBlockingQueue()
    private val states: MutableMap <String, State> = HashMap()
    private val stateStack : Deque<State> = ArrayDeque()
    private lateinit var currentState: State

    override fun run() {
        transition(getState(initialState), StartupMessage())
        while (true) {
            when (val message = messageQueue.take()) {
                is TransitionMessage -> {
                    transition(getState(message.state), message.message)
                }
                is StopMessage -> {
                    break
                }
                else -> {
                    process(message, currentState).transition?.let {
                        if (it.clearQueue) clearQueue()
                        messageQueue.put(it)
                    }
                }
            }
        }
        currentState.leave()
        stateStack.forEach { it.leave() }
    }

    fun clearQueue() {
        messageQueue.removeIf { it !is StopMessage }
    }

    /**
     * Add an already instantiated state to the state machine.
     */
    fun prepareState(state: State) {
        val name = state::class.qualifiedName ?: throw Exception("Could not get qualified name of $state")
        if (states.containsKey(name)) throw Exception("$name has already been added.")
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

    private fun transition(newState: State, message: Message?) {
        val newStateStack = getParentStates(newState)

        // check for any potential parent states we have now left behind
        if (message !is StartupMessage) {
            currentState.leave()
            stateStack.filter { !newStateStack.contains(it) }
                      .forEach { it.leave() }
        }

        // check for any potential parent states we are now entering
        newStateStack.filter { !stateStack.contains(it) }
                     .reversed()
                     .forEach { enterState(it, message) }

        // refresh the state stack to reflect our current position
        stateStack.clear()
        stateStack.addAll(newStateStack)

        // enter our current state
        currentState = newState
        enterState(newState, message)
    }

    private fun enterState(state: State, message: Message?) {
        val result = state.enter(message)
        if (result.transition == null) return
        if (result.transition.clearQueue) clearQueue()
        messageQueue.put(result.transition)
    }

    private fun getParentStates(state: State): Deque<State> {
        val parent = state.getParentState()
        return if (parent == null) {
            ArrayDeque()
        } else {
            val parentState = getState(parent)
            val deque = getParentStates(parentState)
            if (deque.contains(parentState)) {
                throw IllegalStateException("Cyclic parent states for $state")
            }
            deque.push(parentState)
            deque
        }
    }

    private fun getState(kClass: KClass<*>): State {
        val stateName = kClass.qualifiedName ?: throw Exception("Could not retrieve the qualified name of $kClass.")
        val state = states[stateName]
        if (state != null) return state
        val newState = kClass.createInstance()
        if (newState !is State) throw IllegalStateException("$stateName does not extend State.")
        states[stateName] = newState
        return newState
    }

    private fun process(message: Message, state: State): MessageResult {
        val messageResult = state.process(message)
        if (messageResult.handled) return messageResult
        val parent = state.getParentState() ?: return messageResult
        return process(message, getState(parent))
    }
}