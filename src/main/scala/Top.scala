package aha
package axi2sram

/* Scala Imports */
import scopt.OParser

/* Chisel Imports */
import chisel3._
import chisel3.stage.ChiselStage

/*
 * Configuration
 */
case class GenConfig (
    // Width of AXI ID Signals
    IdWidth     : Int       = 4,
    // Width of Address Busses (in bits)
    AddrWidth   : Int       = 32,
    // Width of Data Busses (in bits)
    DataWidth   : Int       = 64,
    // Output Directory
    OutputDir   : String    = "output/"
)

object Main extends App {


    val builder = OParser.builder[GenConfig]
    val parser  = {
        import builder._
        OParser.sequence (
            programName("axi2sram"),
            head("axi2sram", "1.0"),

            opt[Int]('i', "id-width")
                .action((x, c) => c.copy(IdWidth = x))
                .text("width of AXI ID signals"),

            opt[Int]('a', "address-width")
                .action((x, c) => c.copy(AddrWidth = x))
                .text("width of address busses"),

            opt[Int]('d', "data-width")
                .action((x, c) => c.copy(DataWidth = x))
                .text("width of data busses"),

            opt[String]('o', "output-dir")
                .action((x, c) => c.copy(OutputDir = x))
                .text("output directory")
        )
    }

    OParser.parse(parser, args, GenConfig()) match {
        case Some(config) => new ChiselStage().emitVerilog(
            new AXItoSRAM (
                config.IdWidth,
                config.AddrWidth,
                config.DataWidth
            ),
            Array("--target-dir", config.OutputDir)
        )

        case _ => ()
    }
}
