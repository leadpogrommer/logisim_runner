package ru.leadpogrommer.cdm8e.runner

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import java.io.File
import java.nio.charset.Charset
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true");
    val parser = ArgParser("Logisim tester")
    val imgFileName by parser.argument(ArgType.String, description = "Input file")
    val resultFileName by parser.argument(ArgType.String, description = "Output file")
    val timeout by parser.argument(ArgType.Int, description = "Timeout")

    parser.parse(args)

    val runner = Runner()
    val res = runner.run(imgFileName, timeout)

    File(resultFileName).writer(Charset.forName("UTF-8")).let {
        it.write(res)
        it.close()
    }

    exitProcess(0)
}
