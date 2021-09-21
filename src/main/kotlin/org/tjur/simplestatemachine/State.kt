package org.tjur.simplestatemachine

import kotlin.reflect.KClass

abstract class State {

    /**
     * Called when state is entered.
     * @param message If this state was transitioned to accompanied by a message
     * @return A MessageResult that may contain a TransitionMesage, but is otherwise ignored.
     */
    open fun enter(message: Message? = null) = handled()

    /**
     * Called when this state is transitioned out of.
     */
    open fun leave() {}

    /**
     * The entrypoint of Messages into the state.
     *
     * You should extend the Message class with your own to cover your use case.
     *
     * Returns a MessageResult, which tells the state machine if this message was handled,
     * or if it should be directed to a parent state, if this state has one.
     *
     * If the returned MessageResult contains a TransitionMessage, then the state machine will
     * transition to the desired State.
     */
    abstract fun process(message: Message): MessageResult

    /**
     * Return the class of this states parent state.
     * Return null if it does not have one.
     */
    open fun getParentState() : KClass<*>? = null

    /**
     * Helper method that constructs a simple MessageResult telling the processed message was handled.
     */
    fun handled() = MessageResult(true, false, null)

    /**
     * Helper method that constructs a simple MessageResult telling the processed message was not handled.
     */
    fun unhandled() = MessageResult(false)

    /**
     * Helper method for stopping the state machine.
     */
    fun halt() = MessageResult(true, true, StopMessage())

    /**
     * Helper method for transitioning to a different state.
     */
    fun transitionTo(state: KClass<*>) = MessageResult(true, true, TransitionMessage(state, null))

    /**
     * Helper method for transitioning to a different state with more options.
     *
     * @param state Class of the state we want to transition to
     * @param clearQueue By default, we clear the message queue in the state machine when we transition
     *                   to a different state. If you instead put false here, then the remaining messages
     *                   will process before the transition.
     * @param message An optional message to be delivered to the state we want to transition to.
     */
    fun transitionTo(state: KClass<*>, clearQueue: Boolean, message: Message?): MessageResult {
        return MessageResult(true, clearQueue, TransitionMessage(state, message))
    }
}