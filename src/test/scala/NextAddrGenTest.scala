package aha
package axi2sram
package test

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import aha.axi2sram.NextAddrGen

class NextAddrGenWrapper extends Module {
    val CurAddr     = IO(Input(UInt(12.W)))
    val AxLEN       = IO(Input(UInt(8.W)))
    val AxSIZE      = IO(Input(UInt(3.W)))
    val AxBURST     = IO(Input(UInt(2.W)))
    val NextAddr    = IO(Output(UInt(12.W)))

    val m = Module(new NextAddrGen(32))
    m.CurAddr   := CurAddr
    m.AxLEN     := AxLEN
    m.AxSIZE    := AxSIZE
    m.AxBURST   := AxBURST
    NextAddr    := m.NextAddr
}


class NextAddrGenTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "NextAddrGen"

    //
    // INCR Mode
    //
    it should "compute INCR addresses" in {
        test(new NextAddrGenWrapper) { m =>
            for (len <- Seq(0, 99, 255)) {
                for (size <- 0 until 8) {
                    for (addr <- Seq(0, 2048, 3840)) {
                        m.CurAddr.poke(addr.U)
                        m.AxLEN.poke(len.U)
                        m.AxSIZE.poke(size.U)
                        m.AxBURST.poke(1.U)
                        m.NextAddr.expect(FnModel(addr, len, size, 1).U)
                    }
                }
            }
        }
    }

    //
    // FIXED Mode
    //
    it should "compute FIXED addresses" in {
        test(new NextAddrGenWrapper) { m =>
            for (len <- Seq(0, 99, 255)) {
                for (size <- 0 until 8) {
                    for (addr <- Seq(0, 2048, 3840)) {
                        m.CurAddr.poke(addr.U)
                        m.AxLEN.poke(len.U)
                        m.AxSIZE.poke(size.U)
                        m.AxBURST.poke(0.U)
                        m.NextAddr.expect(FnModel(addr, len, size, 0).U)
                    }
                }
            }
        }
    }

    //
    // WRAP Mode
    //

    it should "compute WRAP addresses" in {
        test(new NextAddrGenWrapper) { m =>
            // Scenario 1:
            //  - CurAddr   = 256
            //  - AxLEN     = 1  (2 transfers)
            //  - AxSIZE    = 0  (1-byte transfers)
            var CurAddr = 256
            var AxLEN   = 1
            var AxSIZE  = 0
            var AxBURST = 2

            m.CurAddr.poke(CurAddr.U)
            m.AxLEN.poke(AxLEN.U)
            m.AxSIZE.poke(AxSIZE.U)
            m.AxBURST.poke(AxBURST.U)

            m.NextAddr.expect(FnModel(CurAddr, AxLEN, AxSIZE, AxBURST).U)

            // Scenario 2:
            //  - CurAddr   = 257
            //  - AxLEN     = 1  (2 transfers)
            //  - AxSIZE    = 0  (1-byte transfers)
            CurAddr = 257
            AxLEN   = 1
            AxSIZE  = 0
            AxBURST = 2

            m.CurAddr.poke(CurAddr.U)
            m.AxLEN.poke(AxLEN.U)
            m.AxSIZE.poke(AxSIZE.U)
            m.AxBURST.poke(AxBURST.U)

            m.NextAddr.expect(FnModel(CurAddr, AxLEN, AxSIZE, AxBURST).U)

            // Scenario 3:
            //  - CurAddr   = 256
            //  - AxLEN     = 15 (16 transfers)
            //  - AxSIZE    = 2  (4-byte transfers)
            CurAddr = 256
            AxLEN   = 15
            AxSIZE  = 2
            AxBURST = 2

            m.CurAddr.poke(CurAddr.U)
            m.AxLEN.poke(AxLEN.U)
            m.AxSIZE.poke(AxSIZE.U)
            m.AxBURST.poke(AxBURST.U)

            m.NextAddr.expect(FnModel(CurAddr, AxLEN, AxSIZE, AxBURST).U)

            // Scenario 4:
            //  - CurAddr   = 316
            //  - AxLEN     = 15 (16 transfers)
            //  - AxSIZE    = 2  (4-byte transfers)
            CurAddr = 256
            AxLEN   = 15
            AxSIZE  = 2
            AxBURST = 2

            m.CurAddr.poke(CurAddr.U)
            m.AxLEN.poke(AxLEN.U)
            m.AxSIZE.poke(AxSIZE.U)
            m.AxBURST.poke(AxBURST.U)

            m.NextAddr.expect(FnModel(CurAddr, AxLEN, AxSIZE, AxBURST).U)

            // Scenario 5:
            //  - CurAddr   = 256
            //  - AxLEN     = 15 (16 transfers)
            //  - AxSIZE    = 4  (16-byte transfers)
            CurAddr = 256
            AxLEN   = 15
            AxSIZE  = 4
            AxBURST = 2

            m.CurAddr.poke(CurAddr.U)
            m.AxLEN.poke(AxLEN.U)
            m.AxSIZE.poke(AxSIZE.U)
            m.AxBURST.poke(AxBURST.U)

            m.NextAddr.expect(FnModel(CurAddr, AxLEN, AxSIZE, AxBURST).U)

            // Scenario 6:
            //  - CurAddr   = 496
            //  - AxLEN     = 15 (16 transfers)
            //  - AxSIZE    = 4  (16-byte transfers)
            CurAddr = 496
            AxLEN   = 15
            AxSIZE  = 4
            AxBURST = 2

            m.CurAddr.poke(CurAddr.U)
            m.AxLEN.poke(AxLEN.U)
            m.AxSIZE.poke(AxSIZE.U)
            m.AxBURST.poke(AxBURST.U)

            m.NextAddr.expect(FnModel(CurAddr, AxLEN, AxSIZE, AxBURST).U)

        }
    }
}

object FnModel {
    def apply(CurAddr: Int, AxLEN: Int, AxSIZE: Int, AxBURST: Int) : Int = {
        val nAddr = ((CurAddr >> AxSIZE) + 1) << AxSIZE
        AxBURST match {
            case 1 => nAddr
            case 2 => {
                        val tot = (1 << AxSIZE) * (1 + AxLEN)
                        val wrapBound = (CurAddr/tot)*tot;
                        if (nAddr == wrapBound + tot) wrapBound else nAddr
                      }
            case _ => CurAddr
        }
    }
}
