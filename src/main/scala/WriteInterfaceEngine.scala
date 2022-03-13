package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.experimental.ChiselEnum

/**
 *
 */
class WriteInterfaceEngine( val IdWidth: Int,
                            val AddrWidth: Int,
                            val DataWidth: Int ) extends RawModule {


    // =========================================================================
    // I/O
    // =========================================================================

    // Clock and Reset
    val ACLK        = IO(Input(Clock()))
    val ARESETn     = IO(Input(Bool()))

    // AXI Write Interface View
    val S_AXI       = IO(Flipped(new AXI4WrIntfView(IdWidth, AddrWidth, DataWidth)))

    // Upacked Transfers
    val Ready       = IO(Input(Bool()))
    val Valid       = IO(Output(Bool()))
    val Addr        = IO(Output(UInt(AddrWidth.W)))
    val Data        = IO(Output(UInt(DataWidth.W)))
    val Strb        = IO(Output(UInt((DataWidth/8).W)))
    val Last        = IO(Output(Bool()))

    // =========================================================================
    // Chisel Work-Around for Active-Low Reset
    // =========================================================================

    val reset      = (!ARESETn).asAsyncReset

    // =========================================================================
    // Internal Logic
    // =========================================================================

    withClockAndReset(ACLK, reset) {

        val aw_en       = RegInit(true.B)
        val iAWREADY    = RegInit(false.B)
        val iADDR       = RegInit(0.U(AddrWidth.W))
        val iLEN        = RegInit(0.U(8.W))
        val iSIZE       = RegInit(0.U(3.W))
        val iBURST      = RegInit(0.U(2.W))
        val iBID        = RegInit(0.U(IdWidth.W))
        val iBVALID     = RegInit(false.B)

        val iNextAddr   = NextAddrGen(AddrWidth, iADDR, iLEN, iSIZE, iBURST)

        //
        // aw_en
        //
        when (S_AXI.AW.valid && S_AXI.W.valid && aw_en) {
            aw_en := false.B
        }.elsewhen(S_AXI.B.valid && S_AXI.B.ready) {
            aw_en := true.B
        }

        //
        // Control Information Capture
        //
        when (S_AXI.AW.valid && S_AXI.W.valid && aw_en) {
            iLEN    := S_AXI.AW.bits.LEN
            iSIZE   := S_AXI.AW.bits.SIZE
            iBURST  := S_AXI.AW.bits.BURST
            iBID    := S_AXI.AW.bits.ID
        }

        //
        // Address Generation
        //
        when (S_AXI.AW.valid && S_AXI.W.valid && aw_en) {
            iADDR   := S_AXI.AW.bits.ADDR
        }.elsewhen(~aw_en && Ready && Valid) {
            iADDR   := iNextAddr
        }

        //
        // iBVALID
        //
        when (~aw_en && Ready && Valid && Last) {
            iBVALID := true.B
        }.elsewhen(S_AXI.B.valid && S_AXI.B.ready) {
            iBVALID := false.B
        }
        S_AXI.B.valid       := iBVALID
        S_AXI.B.bits.ID     := iBID
        S_AXI.B.bits.RESP   := 0.U

        //
        // iAWREADY
        //
        when (S_AXI.AW.valid && S_AXI.W.valid && aw_en) {
            iAWREADY    := true.B
        }.otherwise {
            iAWREADY    := false.B
        }
        S_AXI.AW.ready  := iAWREADY

        //
        // WREADY
        //
        S_AXI.W.ready   := ~aw_en && Ready

        //
        // SRAM Outputs
        //
        Valid       := ~aw_en && S_AXI.W.valid
        Addr        := iADDR
        Data        := S_AXI.W.bits.DATA
        Strb        := S_AXI.W.bits.STRB
        Last        := S_AXI.W.bits.LAST


    } // withClockAndReset(ACLK, reset)

}
