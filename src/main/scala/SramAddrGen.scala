package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.util.{Cat, MuxLookup}

class SramAddrGen(val AddrWidth: Int) extends RawModule {

    require(AddrWidth >= 12)

    // =========================================================================
    // I/O
    // =========================================================================

    val CurAddr     = IO(Input(UInt(AddrWidth.W)))
    val Len         = IO(Input(UInt(8.W)))
    val Size        = IO(Input(UInt(3.W)))
    val Burst       = IO(Input(UInt(2.W)))
    val NextAddr    = IO(Output(UInt(AddrWidth.W)))


    // =========================================================================
    // Internal Logic
    // =========================================================================

    val iCurAddr    = CurAddr(11, 0)
    val iNextAddr   = Wire(UInt(12.W))

    // `Size` Numeric Value
    val iSize       = MuxLookup(Size, 0.U, (0 to 7).map(x => x.U -> x.U))

    // Word Address
    val wordAddress = iCurAddr >> iSize

    // INCR Next Addr
    val incrNextAddr = (wordAddress + 1.U) << iSize

    // WRAP Next Addr
    val wrapBound   = Cat(wordAddress(11, 4), (wordAddress(3, 0) & ~Len(3,0))) << iSize
    val totSize     = (Len(3,0) +& 1.U) << iSize
    val wrapNextAddr = Mux(incrNextAddr === wrapBound + totSize, wrapBound, incrNextAddr)

    iNextAddr := MuxLookup(Burst, CurAddr, Seq(0.U -> CurAddr, 1.U -> incrNextAddr, 2.U -> wrapNextAddr))
    NextAddr  := Cat(CurAddr(AddrWidth-1, 12), iNextAddr)

}

object SramAddrGen {
    def apply[T <: Data](AddrWidth: Int, CurAddr: T, Len: T, Size: T, Burst: T) : UInt = {
        val m = Module(new SramAddrGen(AddrWidth))
        m.CurAddr   := CurAddr
        m.Len       := Len
        m.Size      := Size
        m.Burst     := Burst
        m.NextAddr
    }
}
