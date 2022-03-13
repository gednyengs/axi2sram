package aha
package axi2sram

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class Test extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "AXItoSRAM"

    val annos = Seq(WriteVcdAnnotation)

    it should "write data" in {
        test(new AXItoSRAMWrapper(2, 32, 64, 2048, 64, true)).withAnnotations(annos) { dut =>

            val len = 255

            val bfm = new Bfm(dut)
            var resp: Boolean = false
            val rnd = new scala.util.Random
            val data = Seq.fill(len + 1)(0).map(_ => rnd.nextInt(65535))


            fork {
                bfm.WriteAddress(0, len, 3)
                dut.clock.step()
            }.fork {
                bfm.WriteData(data)
            }.fork {
                assert(bfm.WriteResponse())
            }.fork {
                dut.clock.step(4)
                bfm.ReadAddress(0, len, 3)
                dut.clock.step()
            }.fork {
                val rdata = bfm.ReadData()
                dut.clock.step()

                assert(rdata == data)
            }.joinAndStep(dut.clock)
        }
    }
}
