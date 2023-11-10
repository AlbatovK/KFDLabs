package oop.transport

import oop.engine.CompressedAirEngine
import oop.engine.ElectricEngine
import oop.engine.InternalCombustionEngine
import oop.engine.MechanicalEngine

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
