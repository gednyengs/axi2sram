package aha
package axi2sram

/* Chisel Imports */
import chisel3._
import chisel3.util.Decoupled
import chisel3.experimental.dataview._

/**
 * Full AXI4 interface bundle
 *
 * @constructor         constructs an AXI4 interface bundle with the provided
 *                      AXI ID width, address bus width and data bus width
 * @param AddrWidth     the width of AXI address busses (AWADDR and ARADDR)
 * @param DataWidth     the width of AXI data busses (WDATA and RDATA)
 */
class AXI4Intf(val IdWidth: Int,
               val AddrWidth: Int,
               val DataWidth: Int) extends Bundle {

    //
    // Write Address Channel
    //
    val AWID        = Output(UInt(IdWidth.W))
    val AWADDR      = Output(UInt(AddrWidth.W))
    val AWLEN       = Output(UInt(8.W))
    val AWSIZE      = Output(UInt(3.W))
    val AWBURST     = Output(UInt(2.W))
    val AWLOCK      = Output(Bool())
    val AWCACHE     = Output(UInt(4.W))
    val AWPROT      = Output(UInt(3.W))
    val AWVALID     = Output(Bool())
    val AWREADY     = Input(Bool())

    //
    // Write Data Channel
    //
    val WDATA       = Output(UInt(DataWidth.W))
    val WSTRB       = Output(UInt((DataWidth/8).W))
    val WLAST       = Output(Bool())
    val WVALID      = Output(Bool())
    val WREADY      = Input(Bool())

    //
    // Write Response Channel
    //
    val BID         = Input(UInt(IdWidth.W))
    val BRESP       = Input(UInt(2.W))
    val BVALID      = Input(Bool())
    val BREADY      = Output(Bool())

    //
    // Read Address Channel
    //
    val ARID        = Output(UInt(IdWidth.W))
    val ARADDR      = Output(UInt(AddrWidth.W))
    val ARLEN       = Output(UInt(8.W))
    val ARSIZE      = Output(UInt(3.W))
    val ARBURST     = Output(UInt(2.W))
    val ARLOCK      = Output(Bool())
    val ARCACHE     = Output(UInt(4.W))
    val ARPROT      = Output(UInt(3.W))
    val ARVALID     = Output(Bool())
    val ARREADY     = Input(Bool())

    //
    // Read Data Channel
    //
    val RID         = Input(UInt(IdWidth.W))
    val RDATA       = Input(UInt(DataWidth.W))
    val RRESP       = Input(UInt(2.W))
    val RLAST       = Input(Bool())
    val RVALID      = Input(Bool())
    val RREADY      = Output(Bool())
}

/**
 * Bundle providing the write view of an AXI4 interface
 *
 * @constructor         constructs an AXI4 write-view interface bundle with the
 *                      provided AXI ID width, address bus width and data bus width
 * @param IdWidth       the width of AXI ID signals (AWID, BID, ARID, and RID)
 * @param AddrWidth     the width of AXI address busses (AWADDR and ARADDR)
 * @param DataWidth     the width of AXI data busses (WDATA and RDATA)
 */
class AXI4WrIntfView(val IdWidth: Int,
                     val AddrWidth: Int,
                     val DataWidth: Int) extends Bundle {

    val AW      = Decoupled(new AXI4AddrPayload(IdWidth, AddrWidth))
    val W       = Decoupled(new AXI4WrDataPayload(DataWidth))
    val B       = Flipped(Decoupled(new AXI4WrRespPayload(IdWidth)))
}

/**
 * Provide the Chisel DataView implicit converters to allow viewing an AXI4 full
 * interface in write-view
 * {{
 *      val fullAxi   = new AXI4Intf(IdWIdth, AddrWidth, DataWidth)
 *      val writeView = fullAxi.views[AXI4WrIntfView]
 * }}
 */
object AXI4WrIntfView {
    implicit val wrIntfView = PartialDataView[AXI4Intf, AXI4WrIntfView] (

        // Interface Mapping
        axi4 => new AXI4WrIntfView(axi4.IdWidth, axi4.AddrWidth, axi4.DataWidth),

        // Signal Mapping for AW Channel
        _.AWID      -> _.AW.bits.ID,
        _.AWADDR    -> _.AW.bits.ADDR,
        _.AWLEN     -> _.AW.bits.LEN,
        _.AWSIZE    -> _.AW.bits.SIZE,
        _.AWBURST   -> _.AW.bits.BURST,
        _.AWLOCK    -> _.AW.bits.LOCK,
        _.AWCACHE   -> _.AW.bits.CACHE,
        _.AWPROT    -> _.AW.bits.PROT,
        _.AWVALID   -> _.AW.valid,
        _.AWREADY   -> _.AW.ready,

        // Signal Mapping for W Channel
        _.WDATA     -> _.W.bits.DATA,
        _.WSTRB     -> _.W.bits.STRB,
        _.WLAST     -> _.W.bits.LAST,
        _.WVALID    -> _.W.valid,
        _.WREADY    -> _.W.ready,

        // Signal Mapping for B Channel
        _.BID       -> _.B.bits.ID,
        _.BRESP     -> _.B.bits.RESP,
        _.BVALID    -> _.B.valid,
        _.BREADY    -> _.B.ready
    )
}

/**
 * Bundle providing the read view of an AXI4 interface
 *
 * @constructor         constructs an AXI4 read-view interface bundle with the
 *                      provided AXI ID width, address bus width and data bus width
 * @param IdWidth       the width of AXI ID signals (AWID, BID, ARID, and RID)
 * @param AddrWidth     the width of AXI address busses (AWADDR and ARADDR)
 * @param DataWidth     the width of AXI data busses (WDATA and RDATA)
 */
class AXI4RdIntfView(val IdWidth: Int,
                     val AddrWidth: Int,
                     val DataWidth: Int) extends Bundle {

    val AR  = Decoupled(new AXI4AddrPayload(IdWidth, AddrWidth))
    val R   = Flipped(Decoupled(new AXI4RdDataPayload(IdWidth, DataWidth)))
}

/**
 * Provide the Chisel DataView implicit converters to allow viewing an AXI4 full
 * interface in read-view
 * {{
 *      val fullAxi   = new AXI4Intf(IdWIdth, AddrWidth, DataWidth)
 *      val readView = fullAxi.views[AXI4RdIntfView]
 * }}
 */
object AXI4RdIntfView {
    implicit val rdIntfView = PartialDataView[AXI4Intf, AXI4RdIntfView] (

        // Interface Mapping
        axi4 => new AXI4RdIntfView(axi4.IdWidth, axi4.AddrWidth, axi4.DataWidth),

        // Signal Mapping for AR Channel
        _.ARID      -> _.AR.bits.ID,
        _.ARADDR    -> _.AR.bits.ADDR,
        _.ARLEN     -> _.AR.bits.LEN,
        _.ARSIZE    -> _.AR.bits.SIZE,
        _.ARBURST   -> _.AR.bits.BURST,
        _.ARLOCK    -> _.AR.bits.LOCK,
        _.ARCACHE   -> _.AR.bits.CACHE,
        _.ARPROT    -> _.AR.bits.PROT,
        _.ARVALID   -> _.AR.valid,
        _.ARREADY   -> _.AR.ready,

        // Signal Mapping for R Channel
        _.RID       -> _.R.bits.ID,
        _.RDATA     -> _.R.bits.DATA,
        _.RRESP     -> _.R.bits.RESP,
        _.RLAST     -> _.R.bits.LAST,
        _.RVALID    -> _.R.valid,
        _.RREADY    -> _.R.ready
    )
}


/**
 * AXI4 address bus bundle
 *
 * @constructor         constructs axi AXI4 address bundle with the provided
 *                      AXI ID width and address bus width
 * @param IdWidth       the width of AXI ID signals (AWID, BID, ARID, and RID)
 * @param AddrWidth     the width of AXI address busses (AWADDR and ARADDR)
 */
class AXI4AddrPayload(IdWidth: Int, AddrWidth: Int) extends Bundle {
    val ID      = UInt(IdWidth.W)
    val ADDR    = UInt(AddrWidth.W)
    val LEN     = UInt(8.W)
    val SIZE    = UInt(3.W)
    val BURST   = UInt(2.W)
    val LOCK    = Bool()
    val CACHE   = UInt(4.W)
    val PROT    = UInt(3.W)
}

/**
 * AXI4 write data bus bundle
 *
 * @constructor         constructs axi AXI4 write data bundle with the provided
 *                      AXI data bus width
 * @param DataWidth     the width of AXI data busses (WDATA and RDATA)
 */
class AXI4WrDataPayload(DataWidth: Int) extends Bundle {
    val DATA    = UInt(DataWidth.W)
    val STRB    = UInt((DataWidth/8).W)
    val LAST    = Bool()
}

/**
 * AXI4 write response bus bundle
 *
 * @constructor         constructs axi AXI4 write response bundle with the provided
 *                      AXI ID width
 * @param IdWidth       the width of AXI ID signals (AWID, BID, ARID, and RID)
 */
class AXI4WrRespPayload(IdWidth: Int) extends Bundle {
    val ID      = UInt(IdWidth.W)
    val RESP    = UInt(2.W)
}

/**
 * AXI4 read data bus bundle
 *
 * @constructor         constructs axi AXI4 read data bundle with the provided
 *                      AXI ID width and data bus width
 * @param IdWidth       the width of AXI ID signals (AWID, BID, ARID, and RID)
 * @param DataWidth     the width of AXI data busses (WDATA and RDATA)
 */
class AXI4RdDataPayload(IdWidth: Int, DataWidth: Int) extends Bundle {
    val ID      = UInt(IdWidth.W)
    val DATA    = UInt(DataWidth.W)
    val RESP    = UInt(2.W)
    val LAST    = Bool()
}
