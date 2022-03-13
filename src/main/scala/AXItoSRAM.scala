package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.experimental.dataview._
import chisel3.util.{isPow2, Log2}

/**
 *
 */
class AXItoSRAM(val AxiIdWidth           : Int,
                val AxiAddrWidth         : Int,
                val DataWidth            : Int,
                val SramDepth            : Int,
                val SramHasByteEnable    : Boolean) extends RawModule {

    require(isPow2(DataWidth))
    require(isPow2(SramDepth))

    // =========================================================================
    // I/O
    // =========================================================================

    // Clock and Reset
    val ACLK        = IO(Input(Clock()))
    val ARESETn     = IO(Input(Bool()))

    // AXI4 Interface
    val S_AXI       = IO(Flipped(new AXI4Intf(AxiIdWidth, AxiAddrWidth, DataWidth)))

    // SRAM Interface
    val SramAddrWidth = log2(SramDepth)
    val SRAM        = IO(new SRAMSingleIntf(SramAddrWidth, DataWidth, SramHasByteEnable))


    // =========================================================================
    // Chisel Work-Around for Active-Low Reset
    // =========================================================================

    val reset       = (!ARESETn).asAsyncReset

    // =========================================================================
    // Internal Logic
    // =========================================================================

    val axi4RdView  = S_AXI.viewAs[AXI4RdIntfView]
    val axi4WrView  = S_AXI.viewAs[AXI4WrIntfView]

    val sramAddr    = Wire(UInt(AxiAddrWidth.W))

    withClockAndReset(ACLK, reset) {

        val wrIE    = Module(new WriteInterfaceEngine(AxiIdWidth, AxiAddrWidth, DataWidth))
        val rdIE    = Module(new ReadInterfaceEngine(AxiIdWidth, AxiAddrWidth, DataWidth))
        val arbiter = Module(new SRAMArbiter)

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
        arbiter.WrLast  := wrIE.Last
        arbiter.RdValid := rdIE.Valid
        arbiter.RdLast  := rdIE.Last

        sramAddr        := Mux(arbiter.Selected, wrIE.Addr, rdIE.Addr)
        SRAM.ADDR       := sramAddr(SramAddrWidth-1, 0)
        SRAM.CEn        := ~((arbiter.Selected & wrIE.Valid) | (~arbiter.Selected & rdIE.Valid))
        SRAM.WDATA      := wrIE.Data
        SRAM.WEn        := ~arbiter.Selected | ~wrIE.Valid

        SRAM.WBEn.foreach (x => x := ~wrIE.Strb)

    } // withClockAndReset(ACLK, reset)
}
