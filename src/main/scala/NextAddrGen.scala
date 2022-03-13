package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.util.{Cat, MuxLookup}

class NextAddrGen(val AddrWidth: Int) extends RawModule {

    require(AddrWidth >= 12)

    // =========================================================================
    // I/O
    // =========================================================================

    val CurAddr     = IO(Input(UInt(AddrWidth.W)))
    val AxLEN       = IO(Input(UInt(8.W)))
    val AxSIZE      = IO(Input(UInt(3.W)))
    val AxBURST     = IO(Input(UInt(2.W)))
    val NextAddr    = IO(Output(UInt(AddrWidth.W)))


    // =========================================================================
    // Internal Logic
    // =========================================================================

    val iCurAddr    = CurAddr(11, 0)
    val iNextAddr   = Wire(UInt(12.W))

    // AxSIZE Numeric Value
    val size        = MuxLookup(AxSIZE, 0.U, (0 to 7).map(x => x.U -> x.U))

    // Word Address
    val wordAddress = iCurAddr >> size

    // INCR Next Addr
    val incrNextAddr = (wordAddress + 1.U) << size

    // WRAP Next Addr
    val wrapBound   = Cat(wordAddress(11, 4), (wordAddress(3, 0) & ~AxLEN(3,0))) << size
    val totSize     = (AxLEN(3,0) +& 1.U) << size
    val wrapNextAddr = Mux(incrNextAddr === wrapBound + totSize, wrapBound, incrNextAddr)

    iNextAddr := MuxLookup(AxBURST, CurAddr, Seq(0.U -> CurAddr, 1.U -> incrNextAddr, 2.U -> wrapNextAddr))
    NextAddr  := Cat(CurAddr(AddrWidth-1, 12), iNextAddr)

}

object NextAddrGen {
    def apply[T <: Data](AddrWidth: Int, CurAddr: T, AxLEN: T, AxSIZE: T, AxBURST: T) : UInt = {
        val m = Module(new NextAddrGen(AddrWidth))
        m.CurAddr   := CurAddr
        m.AxLEN     := AxLEN
        m.AxSIZE    := AxSIZE
        m.AxBURST   := AxBURST
        m.NextAddr
    }
}
