package org.tjur.simplestatemachine

import kotlin.reflect.KClass

class StartingState : State() {
    override fun enter(message: Message?): MessageResult {
        println("Entered StartingState, but transitioning to SecondState")
        return handled(SecondState::class)
    }

    override fun leave() {
        println("Exiting StartingState")
    }

    override fun getParentState() = SecondState::class

    override fun process(message: Message): MessageResult {
        return unhandled()
    }

}

class SecondState: State() {
    override fun enter(message: Message?): MessageResult {
        println("Entered SecondState")
        return handled()
    }

    override fun process(message: Message): MessageResult {
        println("Got message")
        return handled()
    }

}

fun main(args:Array<String>){
    val stateMachine = SimpleStateMachine(StartingState::class)

    stateMachine.run()
}