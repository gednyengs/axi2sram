package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.util.{Cat, is, switch}

class SRAMArbiter extends RawModule {

    // Clock and Reset
    val ACLK        = IO(Input(Clock()))
    val ARESETn     = IO(Input(Bool()))

    // Write Channel
    val WrValid     = IO(Input(Bool()))
    val WrLast      = IO(Input(Bool()))
    val WrReady     = IO(Output(Bool()))

    // Read Channel
    val RdValid     = IO(Input(Bool()))
    val RdLast      = IO(Input(Bool()))
    val RdReady     = IO(Output(Bool()))

    // Arbiter Status
    val Selected    = IO(Output(Bool()))

    // =========================================================================
    // Chisel Work-Around for Active-Low Reset
    // =========================================================================

    val reset       = (!ARESETn).asAsyncReset


    // =========================================================================
    // Internal Logic
    // =========================================================================

    withClockAndReset(ACLK, reset) {

        // Read granted at reset
        //  Read    -> grant = 0
        //  Write   -> grant = 1
        val grant       = RegInit(false.B)
        val grantEn     = Wire(Bool())

        //
        // Arbitration
        //
        when (grantEn) {
            switch (Cat(WrValid, RdValid)) {
                is (0.U) {
                    grant := grant
                }
                is (1.U) {
                    grant := false.B
                }
                is (2.U) {
                    grant := true.B
                }
                is (3.U) {
                    grant := ~grant
                }
            }
        }

        //
        // Enable Arbitration
        //
        grantEn     :=  (grant && (~WrValid | WrLast)) | (~grant && (~RdValid | RdLast))

        Selected    := grant
        WrReady     := grant
        RdReady     := ~grant

    } // withClockAndReset(ACLK, reset)

}
