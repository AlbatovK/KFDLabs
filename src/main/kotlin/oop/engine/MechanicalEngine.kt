package oop.engine

abstract class MechanicalEngine(private val power: UInt) : Engine {

    open fun description() = javaClass.name + power.toString()

    fun getPower() = power

}
