package oop.engine

class InternalCombustionEngine(
    power: UInt,
    private val flammableFuelType: FlammableFuelType
) : MechanicalEngine(power) {

    enum class FlammableFuelType {
        DIESEL,
        PETROL,
        BENZINE,
        GAS,
    }

    override fun description() = super.description() + flammableFuelType.name

    override fun startUp() = println(description() + " fired up the fuel")

    override fun stop() = println(description() + " stopped fuel intake")

    fun getFuelType() = flammableFuelType
}
