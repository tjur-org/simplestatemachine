package org.tjur.simplestatemachine

import kotlin.reflect.KClass

/**
 * A message signalling a transition to a different state
 *
 * @param state The state to transition to
 * @param clearQueue If we want to clear the message queue upon transitioning
 * @param message An optional message to deliver to the transitioned state
 */
class TransitionMessage(
    val state: KClass<*>,
    val clearQueue: Boolean,
    val message: Message?
) : Message()