package org.test

import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {
  override fun onEnable() {
    println("Enabled")
  }
  override fun onDisable() {
    println("Disabled")
  }
}
