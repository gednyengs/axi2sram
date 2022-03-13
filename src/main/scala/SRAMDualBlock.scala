package aha
package axi2sram

/* Chisel Imports */
import chisel3._

/**
 *
 */

class SRAMDualBlock(val AddrWidth: Int,
                    val DataWidth: Int,
                    val HasByteEnable: Boolean ) extends RawModule {

    // =========================================================================
    // I/O
    // =========================================================================

    // Write Engine Interface
    val WrReady     = IO(Output(Bool()))
    val WrValid     = IO(Input(Bool()))
    val WrAddr      = IO(Input(UInt(AddrWidth.W)))
    val WrData      = IO(Input(UInt(DataWidth.W)))
    val WrStrb      = IO(Input(UInt((DataWidth/8).W)))
    val WrLast      = IO(Input(Bool()))

    // Read Engine Interface
    val RdReady     = IO(Output(Bool()))
    val RdValid     = IO(Input(Bool()))
    val RdAddr      = IO(Input(UInt(AddrWidth.W)))
    val RdData      = IO(Output(UInt(DataWidth.W)))
    val RdLast      = IO(Input(Bool()))

    // SRAM Interface
    val SRAM        = IO(new SRAMDualIntf(AddrWidth, DataWidth, HasByteEnable))

    // =========================================================================
    // Internal Logic
    // =========================================================================

    WrReady := false.B
    RdReady := false.B
    RdData  := DontCare
    SRAM <> DontCare

}
