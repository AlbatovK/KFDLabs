interface Engine {

    fun startUp()

    fun stop()

}

abstract class MechanicalEngine(private val power: UInt) : Engine {

    open fun description() = javaClass.name + power.toString()

    fun getPower() = power

}

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

class ElectricEngine(
    power: UInt,
    private val rotPerSec: UInt
) : MechanicalEngine(power) {

    override fun description() = super.description() + rotPerSec.toString()

    override fun startUp() = println(description() + " started rotating")

    override fun stop() = println(description() + " stopped rotating")

    fun getRotationPerSecond() = rotPerSec

}

class CompressedAirEngine(
    power: UInt,
    private val gasPressure: UInt
) : MechanicalEngine(power) {

    override fun description() = super.description() + gasPressure.toString()

    override fun startUp() = println(description() + " started pressuring gas")

    override fun stop() = println(description() + " released gas")

    fun getGasPressure() = gasPressure

}

interface IMovable {

    fun move(distance: ULong)

}

abstract class Transport(protected val engine: MechanicalEngine) : IMovable {

    fun description() = javaClass.name + engine.description()

}

sealed class FlyingTransport(engine: MechanicalEngine) : Transport(engine) {

    override fun move(distance: ULong) {
        print("Taking off")
        engine.startUp()
        print("Flying for $distance")
        engine.stop()
        print("Descending")
    }

    class AirPlanner(engine: CompressedAirEngine) : FlyingTransport(engine)

    class AirPlane(engine: InternalCombustionEngine) : FlyingTransport(engine)

    class Jet(engine: ElectricEngine) : FlyingTransport(engine)

}

sealed class GroundTransport(engine: MechanicalEngine) : Transport(engine) {

    override fun move(distance: ULong) {
        print("Setting off")
        engine.startUp()
        print("Driving for $distance")
        engine.stop()
        print("Slowing down")
    }

    class Motorcycle(engine: InternalCombustionEngine) : GroundTransport(engine)

    class ElectricCar(engine: ElectricEngine) : GroundTransport(engine)

}


fun main() {
    val transportList = listOf("Motorcycle", "ElectricCar", "Jet", "AirPlane", "AirPlanner")
    println("Select transport type: ${transportList.joinToString(", ")}")

    readlnOrNull()?.let { input ->

        if (input !in transportList)
            println("Unknown transport type")

        val transport = when (input) {
            "Motorcycle" -> GroundTransport.Motorcycle(
                engine = InternalCombustionEngine(300U, InternalCombustionEngine.FlammableFuelType.BENZINE)
            )

            "ElectricCar" -> GroundTransport.ElectricCar(
                engine = ElectricEngine(100U, 2000U)
            )

            "Jet" -> FlyingTransport.Jet(
                engine = ElectricEngine(500U, 3000U)
            )

            "AirPlanner" -> FlyingTransport.AirPlanner(
                engine = CompressedAirEngine(300U, 100000U)
            )

            "AirPlane" -> FlyingTransport.AirPlane(
                engine = InternalCombustionEngine(600U, InternalCombustionEngine.FlammableFuelType.GAS)
            )

            else -> {
                return
            }
        }

        println(transport.description())
    }
}




