// See LICENSE.SiFive for license details.

package ictfreedom.system

import Chisel._
import chisel3.{Flipped, Module}
import chisel3.core.Input
import freechips.rocketchip.config.{Field, Parameters}
import ictfreedom.device._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.util.DontTouch
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.util.{DontTouch, HeterogeneousBag}
import freechips.rocketchip.jtag.JTAGIO

case object ZynqAdapterBase extends Field[BigInt]

/** Top module wrapper which connects Zynq adapter module with PYNQDesign module*/
class PYNQTop (implicit p: Parameters) extends LazyModule{
    val address = p(ZynqAdapterBase)
    val config = p(ExtIn)
    val target = LazyModule(new PYNQDesign)
    val adapter = LazyModule(new ZynqAdapter(address, config.get))

    lazy val module = new LazyModuleImp(this) {
      val io = IO(new Bundle{
          val ps_axi_slave = Flipped(adapter.module.axi)
          val front_axi = Flipped(target.module.l2_frontend_bus_axi4.head.cloneType)
          val mac_int = Input(Bool())
          val sdio_int = Input(Bool())
          val uart_int = Input(Bool())
          val usb_int = Input(Bool())
          val jtag = Flipped(new JTAGIO)
          val gpio = new GPIOBundle
      })

      val mem_axi = target.module.zynqmem_axi4.map(x => IO(x.head.cloneType))
      val mmio_axi = target.module.zynqmmio_axi4.map(x => IO(x.head.cloneType)) 

      adapter.module.io.pc := target.module.hcpftest
      if (target.module.debug.systemjtag.isEmpty) {
        Debug.tieoffDebug(target.module.debug)
      }
      target.module.debug.systemjtag.foreach { djtag =>
        djtag.jtag <> io.jtag
        djtag.mfr_id := p(JtagDTMKey).idcodeManufId.U(11.W)
        djtag.reset := adapter.module.io.sys_reset
      }
      target.module.mac_int := io.mac_int
      target.module.sdio_int := io.sdio_int
      target.module.uart_int := io.uart_int
      target.module.usb_int := io.usb_int
      (mem_axi zip target.module.zynqmem_axi4) foreach { case (io, axi) => io <> axi.head }
      (mmio_axi zip target.module.zynqmmio_axi4) foreach { case (io, axi) => io <> axi.head }
      target.module.l2_frontend_bus_axi4.foreach { frontio =>
        frontio <> io.front_axi
      }
      adapter.module.axi <> io.ps_axi_slave
      target.module.reset := adapter.module.io.sys_reset | target.module.debug.ndreset
      io.gpio <> target.module.gpio
    }
}

/** Example Top with periphery devices and ports, and a Rocket subsystem */
class PYNQDesign(implicit p: Parameters) extends RocketSubsystem
    with HasPeripheryHCPF
    with HasAsyncExtInterrupts
	with HasMAC
    with HasUARTPort
    with HasUSBPort
    with HasSLCRPort
    with HasSDIOPort
    with HasGPIOPort
    with CanHaveMasterAXI4MemPort
    with CanHaveSlaveAXI4Port
    with CanHaveZynqMasterAXI4MemPort
    with CanHaveZynqMasterAXI4MMIOPort
    with HasPeripheryBootROM {
  override lazy val module = new PYNQDesignModuleImp(this)
}

class PYNQDesignModuleImp[+L <: PYNQDesign](_outer: L) extends RocketSubsystemModuleImp(_outer)
    with HasPeripheryHCPFModuleImp
    with HasRTCModuleImp
	with HasMACPortModuleImp
    with HasUARTPortModuleImp
    with HasUSBPortModuleImp
    with HasSLCRPortModuleImp
    with HasSDIOPortModuleImp
    with HasGPIOPortModuleImp
    with HasExtInterruptsModuleImp
    with CanHaveMasterAXI4MemPortModuleImp
    with CanHaveSlaveAXI4PortModuleImp
    with CanHaveZynqMasterAXI4MemPortModuleImp
    with CanHaveZynqMasterAXI4MMIOPortModuleImp
    with HasPeripheryBootROMModuleImp
    with DontTouch{
    global_reset_vector := 0x10000.U
}
