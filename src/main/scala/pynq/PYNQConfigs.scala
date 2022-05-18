// See LICENSE.SiFive for license details.
// See LICENSE.Berkeley for license details.

package ictfreedom.system

import freechips.rocketchip.config.Config
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.devices.tilelink.BootROMParams
import freechips.rocketchip.tile._
import ictfreedom.device._

class WithZynqAdapter extends Config((site, here, up) => {
  case ZynqAdapterBase => BigInt(0x43C00000L)
})

class WithPYNQBootrom extends Config((site, here, up) => {
  case BootROMParams => BootROMParams(contentFileName = "./bootrom/pynq/bootrom.img")
})

class WithPYNQSIMBootrom extends Config((site, here, up) => {
  case BootROMParams => BootROMParams(contentFileName = "./bootrom/pynq-sim/bootrom.img")
})

class WithPYNQMemPort extends Config((site, here, up) => {
  // Parameters correspond to PYNQTop.mem_axi(*) respectively
  case ZynqExtMem => Seq(
    ZynqMasterPortParams(
      base = x"1000_0000",
      size = x"1000_0000",
      tlMapBase = Some(x"5000_0000"), // base address in RV core's view
      beatBytes = site(MemoryBusKey).beatBytes,
      idBits = 4),
    ZynqMasterPortParams(
      base = x"2000_0000",
      size = x"2000_0000",
      tlMapBase = Some(x"6000_0000"), // base address in RV core's view
      beatBytes = site(MemoryBusKey).beatBytes,
      idBits = 4))
})

class WithPYNQMMIOPort extends Config((site, here, up) => {
  // Parameters correspond to PYNQTop.mmio_axi(*) respectively
  case ZynqExtBus => Seq(
    ZynqMasterPortParams(
      base = x"e000_0000",
      size = x"0030_0000",
      beatBytes = site(MemoryBusKey).beatBytes,
      idBits = 4,
      executable = false),
    ZynqMasterPortParams(
      base = x"f800_0000",
      size = x"0000_0c00",
      beatBytes = site(MemoryBusKey).beatBytes,
      idBits = 4,
      executable = false))
})

class WithJtagDTMKey extends Config((site, here, up) => {
  case JtagDTMKey => new JtagDTMConfig (
    idcodeVersion = 2,
    idcodePartNum = 0x000,
    idcodeManufId = 0x480,
    debugIdleCycles = 5)
})

class BaseConfig extends Config(
  new WithPYNQMemPort() ++
  new WithPYNQMMIOPort() ++
  new WithDefaultSlavePort() ++
  new WithTimebase(BigInt(1000000)) ++ // 1 MHz
  new WithDTS("freechips,rocketchip-unknown", Nil) ++
  new WithNExtTopInterrupts(2) ++
  new WithJtagDTM() ++
  new WithJtagDTMKey() ++
  new WithDebugSBA() ++
  new BaseSubsystemConfig().alter((site, here, up) => {
    case PeripheryBusKey => up(PeripheryBusKey, site).copy(
      dtsFrequency = Some(BigInt(50000000))) // 50 MHz
  })
)

class PYNQFPGAConfig extends Config(new WithZynqAdapter ++ new WithNBigCores(1) ++ new WithPYNQBootrom ++ new BaseConfig )
class PYNQSIMConfig extends Config(new WithZynqAdapter ++ new WithNBigCores(1) ++ new WithPYNQSIMBootrom ++ new BaseConfig )

class DefaultConfig extends Config(new WithNBigCores(1) ++ new BaseConfig)

//** TODO: Boot two cores system*/
class WithPYNQDualCoreBootrom extends Config((site, here, up) => {
  case BootROMParams => BootROMParams(contentFileName = "./bootrom/pynq-dualcore/bootrom.img")
})

class PYNQDualCoreConfig extends Config(new WithZynqAdapter ++ new WithNSmallCores(2) ++ new WithPYNQDualCoreBootrom ++ new BaseConfig)
