# AXI4 to SRAM Interface Converter

This utility converter helps connect a device with an SRAM interface to an
AXI4 bus/interface

## Features

This interface converter is parameterized over:
- the size AXI ID ports
- the size of the address ports
- the size of the data ports

It also:
- supports write byte enable signals
- supports single-port SRAM interface

## Chisel Generation

To find all supported command-line arguments, run `sbt 'run --help'` in the top-level directory

To generate, for example, an interface converter with AXI ID widths of 4 bits, 32-bit address busses, and 64-bit data busses:
- `sbt 'run -i 4 -a 32 -d 64 -o <OUTPUT_DIR_PATH>'`
- or `sbt 'run --id-width 4 --address-width 32 --data-width 64 --output-dir <OUTPUT_DIR_PATH>'`
