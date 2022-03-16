package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.experimental.dataview._
import chisel3.util.{isPow2, Log2}

/**
 *
 */
class AXItoSRAM(val IdWidth           : Int,
                val AddrWidth         : Int,
                val DataWidth         : Int) extends RawModule {

    require(isPow2(DataWidth))

    // =========================================================================
    // I/O
    // =========================================================================

    // Clock and Reset
    val ACLK        = IO(Input(Clock()))
    val ARESETn     = IO(Input(Bool()))

    // AXI4 Interface
    val S_AXI       = IO(Flipped(new AXI4Intf(IdWidth, AddrWidth, DataWidth)))

    // SRAM Interface
    val SRAM        = IO(new SRAMSingleIntf(AddrWidth, DataWidth))


    // =========================================================================
    // Chisel Work-Around for Active-Low Reset
    // =========================================================================

    val reset       = (!ARESETn).asAsyncReset

    // =========================================================================
    // Internal Logic
    // =========================================================================

    val axi4RdView  = S_AXI.viewAs[AXI4RdIntfView]
    val axi4WrView  = S_AXI.viewAs[AXI4WrIntfView]

    withClockAndReset(ACLK, reset) {

        val wrIE    = Module(new WriteEngine(IdWidth, AddrWidth, DataWidth))
        val rdIE    = Module(new ReadEngine(IdWidth, AddrWidth, DataWidth))
        val arbiter = Module(new SramArbiter)

        wrIE.ACLK       := ACLK
        wrIE.ARESETn    := ARESETn
        wrIE.S_AXI      <> axi4WrView
        wrIE.Ready      := arbiter.WrReady

        rdIE.ACLK       := ACLK
        rdIE.ARESETn    := ARESETn
        rdIE.S_AXI      <> axi4RdView
        rdIE.Ready      := arbiter.RdReady
        rdIE.Data       := SRAM.RDATA

        arbiter.ACLK    := ACLK
        arbiter.ARESETn := ARESETn
        arbiter.WrValid := wrIE.Valid
        arbiter.RdValid := rdIE.Valid

        SRAM.ADDR       := Mux(wrIE.Ready, wrIE.Addr, rdIE.Addr)
        SRAM.CEn        := ~((wrIE.Ready & wrIE.Valid) | (rdIE.Ready & rdIE.Valid))
        SRAM.WDATA      := wrIE.Data
        SRAM.WEn        := ~(wrIE.Ready & wrIE.Valid)
        SRAM.WBEn       := ~wrIE.Strb


    } // withClockAndReset(ACLK, reset)
}
