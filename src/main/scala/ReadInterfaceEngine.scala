package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.experimental.ChiselEnum

/**
 *
 */
class ReadInterfaceEngine(  val IdWidth: Int,
                            val AddrWidth: Int,
                            val DataWidth: Int ) extends RawModule {


    // =========================================================================
    // I/O
    // =========================================================================

    // Clock and Reset
    val ACLK        = IO(Input(Clock()))
    val ARESETn     = IO(Input(Bool()))

    // AXI Read Interface View
    val S_AXI       = IO(Flipped(new AXI4RdIntfView(IdWidth, AddrWidth, DataWidth)))

    // Upacked Transfers
    val Ready       = IO(Input(Bool()))
    val Valid       = IO(Output(Bool()))
    val Addr        = IO(Output(UInt(AddrWidth.W)))
    val Data        = IO(Input(UInt(DataWidth.W)))
    val Last        = IO(Output(Bool()))

    // =========================================================================
    // Chisel Work-Around for Active-Low Reset
    // =========================================================================

    val reset      = (!ARESETn).asAsyncReset

    // =========================================================================
    // Internal Logic
    // =========================================================================

    withClockAndReset(ACLK, reset) {

        val ar_en       = RegInit(true.B)
        val iADDR       = RegInit(0.U(AddrWidth.W))
        val iLEN        = RegInit(0.U(8.W))
        val iSIZE       = RegInit(0.U(3.W))
        val iBURST      = RegInit(0.U(2.W))
        val iRID        = RegInit(0.U(IdWidth.W))
        val iRVALID     = RegInit(false.B)
        val iARREADY    = RegInit(false.B)
        val counter     = RegInit(0.U(8.W))
        val iValid      = RegInit(false.B)
        val iData       = RegInit(0.U(DataWidth.W))
        val rValid      = RegNext(Valid, init=false.B)
        val rReady      = RegNext(Ready, init=false.B)

        val iNextAddr   = NextAddrGen(AddrWidth, iADDR, iLEN, iSIZE, iBURST)

        //
        // ar_en
        //
        when (S_AXI.AR.valid && ar_en) {
            ar_en := false.B
        }.elsewhen(S_AXI.R.valid && S_AXI.R.ready && S_AXI.R.bits.LAST) {
            ar_en := true.B
        }

        //
        // Control Information Capture
        //
        when (S_AXI.AR.valid && ar_en) {
            iLEN    := S_AXI.AR.bits.LEN
            iSIZE   := S_AXI.AR.bits.SIZE
            iBURST  := S_AXI.AR.bits.BURST
            iRID    := S_AXI.AR.bits.ID
        }

        //
        // Counter
        //
        when (S_AXI.AR.valid && ar_en) {
            counter := S_AXI.AR.bits.LEN
        }.elsewhen(Ready && Valid && (counter > 0.U)) {
            counter := counter - 1.U
        }

        //
        // Address Generation
        //
        when (S_AXI.AR.valid && ar_en) {
            iADDR   := S_AXI.AR.bits.ADDR
        }.elsewhen(~ar_en && Ready && Valid) {
            iADDR   := iNextAddr
        }

        //
        // iARREADY
        //
        when (S_AXI.AR.valid && ar_en) {
            iARREADY    := true.B
        }.otherwise {
            iARREADY    := false.B
        }
        S_AXI.AR.ready  := iARREADY

        //
        // iRVALID
        //
        when (Valid && Ready) {
            iRVALID := true.B
        }.elsewhen(S_AXI.R.valid && !S_AXI.R.ready) {
            iRVALID := true.B
        }.elsewhen(S_AXI.R.valid && S_AXI.R.ready) {
            iRVALID := false.B
        }
        S_AXI.R.valid   := iRVALID

        //
        // RRESP, RID, RLAST
        //
        S_AXI.R.bits.RESP   := 0.U
        S_AXI.R.bits.ID     := iRID
        S_AXI.R.bits.LAST   := RegNext((counter === 0.U) && ~ar_en, init=false.B)

        //
        // SRAM Valid
        //
        when (S_AXI.AR.valid && ar_en) {
            iValid  := true.B
        }.elsewhen(~ar_en && Valid && ~Ready) {
            iValid  := true.B
        }.elsewhen(~ar_en && S_AXI.R.ready && S_AXI.R.valid && !S_AXI.R.bits.LAST) {
            iValid  := true.B
        }.otherwise {
            iValid  := false.B
        }

        //
        // Internal Data Storage
        //
        when (rValid && rReady) {
            iData   := Data
        }

        //
        // RDATA
        //
        val dataSelect = RegNext(S_AXI.R.valid && !S_AXI.R.ready, init = false.B)

        when (dataSelect) {
            S_AXI.R.bits.DATA   := iData
        }.otherwise {
            S_AXI.R.bits.DATA   := Data
        }

        //
        // SRAM Outputs
        //
        Valid       := iValid
        Addr        := iADDR
        Last        := counter === 0.U


    } // withClockAndReset(ACLK, reset)

}
