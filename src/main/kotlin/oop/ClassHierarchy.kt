package oop

import oop.engine.CompressedAirEngine
import oop.engine.ElectricEngine
import oop.engine.InternalCombustionEngine
import oop.transport.FlyingTransport
import oop.transport.GroundTransport

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
