package aha
package axi2sram

import chisel3._
import chisel3.stage.ChiselStage

object Main extends App {

    val IdWidth         = 4
    val AddrWidth       = 32
    val DataWidth       = 64
    (new ChiselStage).emitVerilog(
        new AXItoSRAM(  IdWidth,
                        AddrWidth,
                        DataWidth),
        Array("--target-dir", "output/")
    )
}
