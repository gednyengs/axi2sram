package aha

/* Scala Imports */
import scala.math._

package object axi2sram {

    /**
     * Returns log2 of a number rounded up
     *
     * @param x the number of which the clog2 is computed
     * @return the log2 of x rounded up
     */
    def clog2(x: Int) : Int = {
        require(x >= 0)
        ceil(log(x)/log(2)).toInt
    }

    /**
     * Returns log2 of a number
     *
     * @param x the number of which the log2 is computed
     * @return the log2 of x
     */
    def log2(x: Int) : Int = {
        require(x >= 0)
        (log(x)/log(2)).toInt
    }

}
