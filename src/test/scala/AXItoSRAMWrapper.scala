package aha
package axi2sram

import chisel3._

class TestMemory(   val AddrWidth: Int,
                    val DataWidth: Int,
                    val HasByteEnable: Boolean) extends Module {
    val SRAM = IO(Flipped(new SRAMSingleIntf(AddrWidth, DataWidth, HasByteEnable)))

    val mem = SyncReadMem(1 << AddrWidth, UInt(DataWidth.W))

    SRAM.RDATA := DontCare

    when(~SRAM.CEn) {
        val rdwr = mem(SRAM.ADDR)
        when (~SRAM.WEn) { rdwr := SRAM.WDATA }
        .otherwise { SRAM.RDATA := rdwr }
    }
}

class AXItoSRAMWrapper( val AxiIdWidth           : Int,
                        val AxiAddrWidth         : Int,
                        val DataWidth            : Int,
                        val SramDepth            : Int,
                        val SramHasByteEnable    : Boolean) extends Module {

    // AXI4 Interface
    val S_AXI       = IO(Flipped(new AXI4Intf(AxiIdWidth, AxiAddrWidth, DataWidth)))

    // Test Memory
    val SramAddrWidth = log2(SramDepth)
    val mem = Module(new TestMemory(SramAddrWidth, DataWidth, SramHasByteEnable))

    // Axi2Sram
    val inst = Module(new AXItoSRAM(
                            AxiIdWidth,
                            AxiAddrWidth,
                            DataWidth,
                            SramDepth,
                            SramHasByteEnable)
                    )

    inst.ACLK       := clock
    inst.ARESETn    := ~(reset.asBool)
    inst.S_AXI      <> S_AXI
    inst.SRAM       <> mem.SRAM
}
