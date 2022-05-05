// See LICENSE.SiFive for license details.
// See LICENSE.SiFive for license details.

package ictfreedom.system

import Chisel._
import chisel3.Flipped
import chisel3.core.Input
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.Debug
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.subsystem.ExtIn
import ictfreedom.device._
import ictfreedom.device._

class TestHarness()(implicit p: Parameters) extends Module {
  val address = p(ZynqAdapterBase)
  val config = p(ExtIn)

  val dut = Module(LazyModule(new PYNQDesign).module)
  val adapter = Module(LazyModule(new ZynqAdapter(address, config.get)).module)

  val io = new Bundle {
    val success = Bool(OUTPUT)
    val ps_axi_slave = Flipped(adapter.axi.cloneType)
    val sd_int = Input(Bool())
    val uart_int = Input(Bool())
    val mac_int = Input(Bool())
  }
  
    adapter.axi <> io.ps_axi_slave
    adapter.io.pc := dut.hcpftest
    dut.reset := adapter.io.sys_reset
    dut.sdio_int := io.sd_int
    dut.uart_int := io.uart_int
    dut.mac_int := io.mac_int

    dut.dontTouchPorts()
    dut.connectSimAXIMem()
    Debug.tieoffDebug(dut.debug)
    dut.l2_frontend_bus_axi4.foreach(_.tieoff)
    dut.connectSimUART()
}

