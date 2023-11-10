package oop.transport

import oop.engine.ElectricEngine
import oop.engine.InternalCombustionEngine
import oop.engine.MechanicalEngine

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