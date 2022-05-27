package ru.leadpogrommer.cdm8e.runner


import com.cburch.hex.HexModel
import com.cburch.logisim.circuit.Analyze
import com.cburch.logisim.circuit.Circuit
import com.cburch.logisim.circuit.CircuitState
import com.cburch.logisim.file.Loader
import com.cburch.logisim.instance.Instance
import com.cburch.logisim.proj.Project
import com.cburch.logisim.std.memory.Ram
import com.cburch.logisim.std.memory.Rom
//import com.cburch.logisim.std.memory.MemState
import com.cburch.logisim.std.wiring.Pin
import com.google.gson.Gson
import java.io.File
import java.util.concurrent.TimeoutException
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.system.exitProcess


fun CircuitState.getPinValue(pin: Instance): Int {
    val state = this.getInstanceState(pin)
    return Pin.FACTORY.getValue(state).toIntValue()
}


class Runner() {
    fun run(inputFileName: String, timeout: Int): String {
        println("Loading file...")
        val logisimLoader = Loader(null)
        val stream = this.javaClass.getResourceAsStream("/runner.circ")
        val logisimFile = logisimLoader.openLogisimFile(stream)
        val logisimProject = Project(logisimFile)
        val circuit = logisimFile.mainCircuit

        val outputPins = Analyze.getPinLabels(circuit).filter {
            !Pin.FACTORY.isInputPin(it.key)
        }.entries.associate { (k, v) -> v to k }

        val haltPin = outputPins[HALT_PIN]!!
        val circuitState = CircuitState(logisimProject, circuit)

        val propagator = circuitState.propagator

        val romComponent = circuit.nonWires.find { it.factory is Rom }

        propagator.propagate()
        (romComponent!!.factory as Rom).loadImage(circuitState.getInstanceState(romComponent), File(inputFileName))
        propagator.propagate()


        val start = System.currentTimeMillis()

        println("Starting simulation")
        while (true) {
            if (start + timeout < System.currentTimeMillis()) {
                println("Timeout")
                exitProcess(1)
            }
            if (circuitState.getPinValue(haltPin) != 0) {
                break
            }
            propagator.tick()
            propagator.propagate()
        }
        println("Simulation done")

        val registersValues = PIN_NAMES.map { it to circuitState.getPinValue(outputPins[it]!!) }.toMap()


        val contents = getRamContents(circuit, circuitState)

        val  ramData = (0 .. contents.lastOffset).map { contents[it] }

//        logisimLoader.

        val gson = Gson()

        return gson.toJsonTree(registersValues).also {
            it.asJsonObject.add("mem", gson.toJsonTree(ramData))
        }.toString()
    }

    private fun getRamContents(circuit: Circuit, circuitState: CircuitState): HexModel{
        val ramComponent = circuit.nonWires.find { it.factory is Ram }!!
        val getStateMethod = ramComponent.factory::class.functions.find { it.name == "getState" && it.parameters.size == 2 }!!
        getStateMethod.isAccessible = true
        val ramState = getStateMethod.call(ramComponent.factory, circuitState.getInstanceState(ramComponent))
        val kClass = Class.forName("com.cburch.logisim.std.memory.MemState").kotlin
        val contentsProperty = kClass.memberProperties.find { it.name == "contents" }!!
        contentsProperty.isAccessible = true
        return contentsProperty.getter.call(ramState) as HexModel
    }

    companion object {
        val PIN_NAMES = arrayOf("r0", "r1", "r2", "r3", "pc", "ps", "sp")
        const val HALT_PIN = "halt"
    }
}