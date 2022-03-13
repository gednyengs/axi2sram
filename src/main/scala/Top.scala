package aha
package axi2sram

import chisel3._
import chisel3.stage.ChiselStage

object Main extends App {

    val AxiIdWidth      = 2
    val AxiAddrWidth    = 32
    val DataWidth       = 64
    val SramDepth       = (1 << 12)
    val SramHasByteEn   = true
    (new ChiselStage).emitVerilog(
        new AXItoSRAM(  AxiIdWidth,
                        AxiAddrWidth,
                        DataWidth,
                        SramDepth,
                        SramHasByteEn),
        Array("--target-dir", "output/")
    )
}
