package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.experimental.ChiselEnum

/**
 *
 */
class ReadEngine(   val IdWidth: Int,
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

    // =========================================================================
    // Chisel Work-Around for Active-Low Reset
    // =========================================================================

    val reset      = (!ARESETn).asAsyncReset

    // =========================================================================
    // Internal Logic
    // =========================================================================

    withClockAndReset(ACLK, reset) {

        val iAddr       = RegInit(0.U(AddrWidth.W))
        val iAddrValid  = RegInit(false.B)
        val NextAddr    = Wire(UInt(AddrWidth.W))

        val iARReady    = Wire(Bool())
        val iLen        = RegInit(0.U(8.W))
        val iSize       = RegInit(0.U(3.W))
        val iBurst      = RegInit(0.U(2.W))
        val iRID        = RegInit(0.U(IdWidth.W))
        val iRLast      = RegInit(false.B)
        val iRValid     = RegInit(false.B)

        val iValid      = RegInit(false.B)
        val iLast       = Wire(Bool())
        val iCounter    = RegInit(0.U(8.W))

        //
        // iAddrValid
        //

        val iAddrValidNext =    Mux(iARReady & S_AXI.AR.valid,
                                    true.B,
                                    Mux(iRValid & iRLast & S_AXI.R.ready,
                                        false.B,
                                        iAddrValid
                                    )
                                )

        iAddrValid := iAddrValidNext

        //
        // ARREADY
        //

        iARReady            := ~iAddrValid
        S_AXI.AR.ready      := iARReady

        //
        // RID
        //

        val iRIDNext =  Mux(iARReady & S_AXI.AR.valid,
                            S_AXI.AR.bits.ID,
                            Mux(iRValid & iRLast & S_AXI.R.ready,
                                false.B,
                                iRID
                            )
                        )

        iRID                := iRIDNext
        S_AXI.R.bits.ID     := iRID

        //
        // RVALID
        //

        val iRValidNext =   Mux(iValid & Ready,
                                true.B,
                                Mux(iRValid & ~S_AXI.R.ready,
                                    true.B,
                                    Mux(iRValid & S_AXI.R.ready,
                                        false.B,
                                        iRValid
                                    )
                                )
                            )

        iRValid         := iRValidNext
        S_AXI.R.valid   := iRValid

        //
        // RDATA
        //

        S_AXI.R.bits.DATA   := Data

        //
        // RRESP
        //

        S_AXI.R.bits.RESP   := 0.U

        //
        // RLAST
        //

        val iRLastNext =    Mux(iValid & iLast & Ready,
                                true.B,
                                Mux(iRValid & S_AXI.R.ready,
                                    false.B,
                                    iRLast
                                )
                            )
        iRLast              := iRLastNext
        S_AXI.R.bits.LAST   := iRLast

        //
        // iLen, iSize, and iBurst
        //

        val iLenNext =  Mux(iARReady & S_AXI.AR.valid,
                            S_AXI.AR.bits.LEN,
                            Mux(iRValid & iRLast & S_AXI.R.ready,
                                0.U,
                                iLen
                            )
                        )
        iLen := iLenNext

        val iSizeNext =  Mux(iARReady & S_AXI.AR.valid,
                            S_AXI.AR.bits.SIZE,
                            Mux(iRValid & iRLast & S_AXI.R.ready,
                                0.U,
                                iSize
                            )
                        )
        iSize := iSizeNext

        val iBurstNext =  Mux(iARReady & S_AXI.AR.valid,
                            S_AXI.AR.bits.BURST,
                            Mux(iRValid & iRLast & S_AXI.R.ready,
                                0.U,
                                iBurst
                            )
                        )
        iBurst := iBurstNext

        //
        // SRAM Valid
        //

        val iValidNext =    Mux(iARReady & S_AXI.AR.valid,
                                true.B,
                                Mux(iLast & Ready,
                                    false.B,
                                    iValid
                                )
                            )
        iValid  := iValidNext
        Valid   := iValid

        //
        // SRAM Addr
        //

        NextAddr    := SramAddrGen(AddrWidth, iAddr, iLen, iSize, iBurst)

        val iAddrNext = Mux(iARReady & S_AXI.AR.valid,
                            S_AXI.AR.bits.ADDR,
                            Mux(Ready,
                                NextAddr,
                                iAddr
                            )
                        )
        iAddr   := iAddrNext
        Addr    := iAddr

        //
        // iLast
        //

        val iCounterNext =  Mux(iARReady & S_AXI.AR.valid,
                                S_AXI.AR.bits.LEN,
                                Mux(iValid & Ready,
                                    iCounter - 1.U,
                                    iCounter
                                )
                            )
        iCounter    := iCounterNext
        iLast       := iCounter === 0.U

    } // withClockAndReset(ACLK, reset)

}
