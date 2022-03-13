package aha
package axi2sram

/* Chisel Imports */
import chisel3._

/**
 * SRAM Interface for Single-Ported SRAMs
 *
 * @constructor     constructs an SRAM bundle for a single-ported SRAM with the
 *                  provided address width, data width, and optionally byte enables
 * @param AddrWidth the width of the memory row decoder
 * @param DataWidth the width of each word in a row (in bits)
 * @param HasByteEnable whether the memory supports byte enable signals
 *
 * @note The total size of the memory is 2^(AddrWidth + log2(DataWidth)) bits
 */
class SRAMSingleIntf(val AddrWidth: Int,
                     val DataWidth: Int,
                     val HasByteEnable: Boolean) extends Bundle {

    // Chip-Level Interface
    val ADDR        = Output(UInt(AddrWidth.W))
    val CEn         = Output(Bool())

    //
    // Write Interface
    //
    val WDATA       = Output(UInt(DataWidth.W))
    val WEn         = Output(Bool())
    val WBEn        = if (HasByteEnable) Some(Output(UInt((DataWidth/8).W))) else None

    //
    // Read Interface
    //
    val RDATA       = Input(UInt(DataWidth.W))
}

/**
 * SRAM Interface for Dual-Ported SRAMs
 *
 * @constructor     constructs an SRAM bundle for a dual-ported SRAM with the
 *                  provided address width, data width, and optionally byte enables
 * @param AddrWidth the width of the memory row decoder
 * @param DataWidth the width of each word in a row (in bits)
 * @param HasByteEnable whether the memory supports byte enable signals
 *
 * @note The total size of the memory is 2^(AddrWidth + log2(DataWidth)) bits
 */
class SRAMDualIntf(val AddrWidth: Int,
                   val DataWidth: Int,
                   val HasByteEnable: Boolean) extends Bundle {

    // Chip-Level Interface
    val CEn         = Output(Bool())

    //
    // Write Interface
    //
    val WADDR       = Output(UInt(AddrWidth.W))
    val WDATA       = Output(UInt(DataWidth.W))
    val WEn         = Output(Bool())
    val WBEn        = if (HasByteEnable) Some(Output(UInt((DataWidth/8).W))) else None

    //
    // Read Interface
    //
    val RADDR       = Output(UInt(AddrWidth.W))
    val RDATA       = Input(UInt(DataWidth.W))
    val REn         = Output(Bool())
}
