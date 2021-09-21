package org.tjur.simplestatemachine

import kotlin.reflect.KClass

/**
 * A message signalling a transition to a different state
 *
 * @param state The state to transition to
 * @param message An optional message to deliver to the transitioned state
 */
class TransitionMessage(
    val state: KClass<*>,
    val message: Message?
) : Message()