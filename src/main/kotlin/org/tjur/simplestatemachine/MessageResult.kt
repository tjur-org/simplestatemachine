package org.tjur.simplestatemachine

/**
 * A result from processing a message
 *
 * @param handled Set to true if the state has handled the message.
 * @param transition Set a TransitionMessage to signal a transition to a different state.
 */
class MessageResult(val handled: Boolean, val transition: TransitionMessage? = null)