package aha
package axi2sram

import chisel3._
import chisel3.stage.ChiselStage

object Main extends App {
    (new ChiselStage).emitVerilog(
        new AXItoSRAM(2, 32, 64, 2048, 64, true), Array("--target-dir", "output/")
    )
}
