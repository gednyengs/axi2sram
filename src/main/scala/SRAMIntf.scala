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
 *
 * @note The total size of the memory is 2^(AddrWidth + log2(DataWidth)) bits
 */
class SRAMSingleIntf(val AddrWidth: Int,
                     val DataWidth: Int ) extends Bundle {

    // Chip-Level Interface
    val CEn         = Output(Bool())
    val ADDR        = Output(UInt(AddrWidth.W))

    //
    // Write Interface
    //
    val WDATA       = Output(UInt(DataWidth.W))
    val WEn         = Output(Bool())
    val WBEn        = Output(UInt((DataWidth/8).W))

    //
    // Read Interface
    //
    val RDATA       = Input(UInt(DataWidth.W))
}
