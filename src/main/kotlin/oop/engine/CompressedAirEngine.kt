package oop.engine

class CompressedAirEngine(
    power: UInt,
    private val gasPressure: UInt
) : MechanicalEngine(power) {

    override fun description() = super.description() + gasPressure.toString()

    override fun startUp() = println(description() + " started pressuring gas")

    override fun stop() = println(description() + " released gas")

    fun getGasPressure() = gasPressure

}