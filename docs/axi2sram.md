# AXI4 to SRAM Interface Converter

This utility converter helps connect a device with an SRAM-like interface to an
AXI4 bus/interface.

This interface converter is parameterized over:
- the size AXI ID ports
- the size of the address ports
- the size of the data ports
- support for write byte enable signals
- number of read wait states
- single or dual ports
