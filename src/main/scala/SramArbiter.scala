package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.util.{Cat, is, switch}

class SramArbiter extends RawModule {

    // Clock and Reset
    val ACLK        = IO(Input(Clock()))
    val ARESETn     = IO(Input(Bool()))

    // Write Channel
    val WrValid     = IO(Input(Bool()))
    val WrReady     = IO(Output(Bool()))

    // Read Channel
    val RdValid     = IO(Input(Bool()))
    val RdReady     = IO(Output(Bool()))

    // =========================================================================
    // Chisel Work-Around for Active-Low Reset
    // =========================================================================

    val reset       = (!ARESETn).asAsyncReset


    // =========================================================================
    // Internal Logic
    // =========================================================================

    withClockAndReset(ACLK, reset) {

        val choice      = RegInit(false.B)
        val choiceNext  = Wire(Bool())

        //
        // Choice/Grant
        //

        choice          := choiceNext

        //
        // Next Choice
        //

        choiceNext      := DontCare
        switch (Cat(WrValid, RdValid)) {
            is (0.U) {
                choiceNext  := choice
            }
            is (1.U) {
                choiceNext  := false.B
            }
            is (2.U) {
                choiceNext  := true.B
            }
            is (3.U) {
                choiceNext  := ~choice
            }
        }

        //
        // Output Assignments
        //
        WrReady     := choice
        RdReady     := ~choice

    } // withClockAndReset(ACLK, reset)

}
