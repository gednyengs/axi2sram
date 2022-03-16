package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.experimental.ChiselEnum

/**
 *
 */
class WriteEngine( val IdWidth: Int,
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

    // =========================================================================
    // Chisel Work-Around for Active-Low Reset
    // =========================================================================

    val reset      = (!ARESETn).asAsyncReset

    // =========================================================================
    // Internal Logic
    // =========================================================================

    withClockAndReset(ACLK, reset) {

        val iAddr       = RegInit(0.U(AddrWidth.W))
        val NextAddr    = Wire(UInt(AddrWidth.W))
        val iAddrValid  = RegInit(false.B)

        val iAwReady    = Wire(Bool())
        val iLen        = RegInit(0.U(8.W))
        val iSize       = RegInit(0.U(3.W))
        val iBurst      = RegInit(0.U(2.W))
        val iBID        = RegInit(0.U(IdWidth.W))
        val iBValid     = RegInit(false.B)

        val iValid      = Wire(Bool())
        val iLast       = Wire(Bool())

        //
        // iAddrValid
        //

        val iAddrValidNext =    Mux(iAwReady & S_AXI.AW.valid,
                                    true.B,
                                    Mux(iBValid & S_AXI.B.ready,
                                        false.B,
                                        iAddrValid
                                    )
                                )
        iAddrValid := iAddrValidNext

        //
        // AWREADY
        //

        iAwReady        := ~iAddrValid
        S_AXI.AW.ready  := iAwReady

        //
        // WREADY
        //

        S_AXI.W.ready   := iAddrValid & Ready

        //
        // BID
        //

        val iBIDNext =  Mux(iAwReady & S_AXI.AW.valid,
                            S_AXI.AW.bits.ID,
                            Mux(iBValid & S_AXI.B.ready,
                                0.U,
                                iBID
                            )
                        )

        iBID                := iBIDNext
        S_AXI.B.bits.ID     := iBID

        //
        // BRESP
        //

        S_AXI.B.bits.RESP   := 0.U

        //
        // BVALID
        //

        val iBValidNext =   Mux(iValid & iLast & Ready,
                                true.B,
                                Mux(iBValid & S_AXI.B.ready,
                                    false.B,
                                    iBValid
                                )
                            )

        iBValid         := iBValidNext
        S_AXI.B.valid   := iBValid

        //
        // iLen, iSize, and iBurst
        //

        val iLenNext =  Mux(iAwReady & S_AXI.AW.valid,
                            S_AXI.AW.bits.LEN,
                            Mux(iBValid & S_AXI.B.ready,
                                0.U,
                                iLen
                            )
                        )
        iLen := iLenNext

        val iSizeNext = Mux(iAwReady & S_AXI.AW.valid,
                            S_AXI.AW.bits.SIZE,
                            Mux(iBValid & S_AXI.B.ready,
                                0.U,
                                iSize
                            )
                        )
        iSize := iSizeNext

        val iBurstNext = Mux(iAwReady & S_AXI.AW.valid,
                            S_AXI.AW.bits.BURST,
                            Mux(iBValid & S_AXI.B.ready,
                                0.U,
                                iBurst
                            )
                        )
        iBurst := iBurstNext

        //
        // SRAM Valid
        //

        iValid  := iAddrValid & S_AXI.W.valid
        Valid   := iValid

        //
        // SRAM Addr
        //

        NextAddr        := SramAddrGen(AddrWidth, iAddr, iLen, iSize, iBurst)
        val iAddrNext   =   Mux(iAwReady & S_AXI.AW.valid,
                                S_AXI.AW.bits.ADDR,
                                Mux(iValid & Ready,
                                    NextAddr,
                                    iAddr
                                )
                            )
        iAddr   := iAddrNext
        Addr    := iAddr

        //
        // SRAM Strb
        //

        Strb    := S_AXI.W.bits.STRB

        //
        // SRAM Data
        //

        Data    := S_AXI.W.bits.DATA

        //
        // iLast
        //

        iLast   := S_AXI.W.bits.LAST


    } // withClockAndReset(ACLK, reset)

}
