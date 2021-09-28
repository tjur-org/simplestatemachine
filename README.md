# SimpleStateMachine

This is an implementation of a finite state machine in Kotlin.

The main class `SimpleStateMachine` is open and extendable, but should be usable out of the box for most use cases.

## Installing

Add jitpack to your `build.gradle`

````
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
````

Then add this project as a dependency:

````
dependencies {
    implementation 'org.tjur:simplestatemachine:Tag'
}
````

## How to use

Create a class and extend `State`. The only method you are required to override is `process`, which is
where your inputs will enter your state. The method returns a `MessageResult` and there
are already some helper methods you can use, for example, `unhandled()`.

````
class MyFirstState : State() {

    override fun process(message: Message): MessageResult {
        return unhandled()
    }

}
````

The `MessageResult` is used to communicate back to the state machine if the message has been handled
or not. If it wasn't, then the state machine will look for a parent state that may be able to
handle it instead.

You use classes that extend `Message` when you want to communicate with the states in the state machine.

````
class MyMessage(val helloWorldString: String) : Message()
,
...

val message = MyMessage("Hello World!")
stateMachine.process(message)
````

To show that your state has a parent state and which it is, override the `getParentState()` method and
return the parent states class:


````
override fun getParentState() = BiggerState::class
````

## Transitioning to a different state

As a result of handling a message, you can return that you want to transition to a different state:

````
if (message is LaunchMessage) {
    return transitionTo(LaunchState::class)
}
````

By default this will clear the message queue in the state machine.

Parent states matter when transitioning state. Entering a state will also enter the parent states.

## Initializing states

Typically, you only mention states by their class, such as `MyState::class`. The state machine
initializes them lazily as they are accessed and them keeps them, but that assumes that there
are no parameters for the constructor. If you know that your state cannot be this simple or you'd
prefer to initialize them yourself, then you  can instantiate it beforehand and put your instance in
the state machine:

````
val advancedState = AdvancedState(missileLaunchCodes)
stateMachine.prepareState(advancedState)
````

If your state has been prepared this way before the state machine attempts to access it, then it
will use your instance.

## Implementation example

````
class StartingState : State() {
    override fun enter(message: Message?): MessageResult {
        println("Entered StartingState, but transitioning to SecondState")
        return transitionTo(SecondState::class)
    }

    override fun leave() {
        println("Exiting StartingState")
    }

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
````
