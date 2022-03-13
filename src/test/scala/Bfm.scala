package aha
package axi2sram

import chisel3._
import chiseltest._

class Bfm(val dut: AXItoSRAMWrapper) {

    def WriteAddress(addr: Int, len: Int, size: Int): Unit = {
        dut.S_AXI.AWID.poke(0.U)
        dut.S_AXI.AWADDR.poke(addr.U)
        dut.S_AXI.AWLEN.poke(len.U)
        dut.S_AXI.AWSIZE.poke(size.U)
        dut.S_AXI.AWBURST.poke(1.U)
        dut.S_AXI.AWLOCK.poke(false.B)
        dut.S_AXI.AWCACHE.poke(2.U)
        dut.S_AXI.AWPROT.poke(0.U)
        dut.S_AXI.AWVALID.poke(true.B)

        fork.withRegion(Monitor) {
            while(dut.S_AXI.AWREADY.peek().litToBoolean == false) {
                dut.clock.step()
            }
        }.joinAndStep(dut.clock)
    }

    def WriteData(data: Seq[Int]): Unit = {
        val n = data.size

        for (i <- 0 until n) {
            dut.S_AXI.WDATA.poke(data(i).U)
            dut.S_AXI.WSTRB.poke(255.U)
            dut.S_AXI.WVALID.poke(true.B)
            dut.S_AXI.WLAST.poke((i == n-1).B)

            fork.withRegion(Monitor) {
                while(dut.S_AXI.WREADY.peek().litToBoolean == false) {
                    dut.clock.step()
                }
            }.joinAndStep(dut.clock)

            // Force arbitration logic to kick in
            dut.S_AXI.WVALID.poke(false.B)
            dut.clock.step(2)
        }
    }

    def WriteResponse(): Boolean = {
        dut.S_AXI.BREADY.poke(true.B)

        fork.withRegion(Monitor) {
            while(dut.S_AXI.BVALID.peek().litToBoolean == false) {
                dut.clock.step()
            }
        }.joinAndStep(dut.clock)

        dut.S_AXI.BRESP.peek().litValue == 0
    }

    def ReadAddress(addr: Int, len: Int, size: Int): Unit = {
        dut.S_AXI.ARID.poke(0.U)
        dut.S_AXI.ARADDR.poke(addr.U)
        dut.S_AXI.ARLEN.poke(len.U)
        dut.S_AXI.ARSIZE.poke(size.U)
        dut.S_AXI.ARBURST.poke(1.U)
        dut.S_AXI.ARLOCK.poke(false.B)
        dut.S_AXI.ARCACHE.poke(2.U)
        dut.S_AXI.ARPROT.poke(0.U)
        dut.S_AXI.ARVALID.poke(true.B)

        fork.withRegion(Monitor) {
            while(dut.S_AXI.ARREADY.peek().litToBoolean == false) {
                dut.clock.step()
            }
        }.joinAndStep(dut.clock)
    }

    def ReadData(): Seq[Int] = {
        var xs = collection.mutable.ArrayBuffer[Int]()
        var done = false
        dut.S_AXI.RREADY.poke(true.B)
        fork.withRegion(Monitor) {
            while(!done) {
                while(dut.S_AXI.RVALID.peek().litToBoolean == false) {
                    dut.clock.step()
                }
                val newd = dut.S_AXI.RDATA.peek().litValue.intValue
                //println(s"newd = ${newd}")
                xs += newd
                done = dut.S_AXI.RLAST.peek().litToBoolean
                dut.clock.step()
            }
        }.joinAndStep(dut.clock)
        xs.toSeq
    }

}
