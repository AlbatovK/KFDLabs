package oop.engine

class ElectricEngine(
    power: UInt,
    private val rotPerSec: UInt
) : MechanicalEngine(power) {

    override fun description() = super.description() + rotPerSec.toString()

    override fun startUp() = println(description() + " started rotating")

    override fun stop() = println(description() + " stopped rotating")

    fun getRotationPerSecond() = rotPerSec

}