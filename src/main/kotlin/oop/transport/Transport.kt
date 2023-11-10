package oop.transport

import oop.engine.MechanicalEngine

abstract class Transport(protected val engine: MechanicalEngine) : IMovable {

    fun description() = javaClass.name + engine.description()
}
