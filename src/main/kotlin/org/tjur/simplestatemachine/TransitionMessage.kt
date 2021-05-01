package org.tjur.simplestatemachine

import kotlin.reflect.KClass

class TransitionMessage(
    val state: KClass<*>,
    val clearQueue: Boolean,
    val message: Message?
) : Message()