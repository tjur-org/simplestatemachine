package org.tjur.simplestatemachine

/**
 * A result from processing a message
 *
 * @param handled Set to true if the state has handled the message.
 * @param clearQueue Set to true if we want to clear the queued messages
 * @param message A new message to be handled by the machine.
 */
class MessageResult(val handled: Boolean, val clearQueue: Boolean = false, val message: Message? = null)