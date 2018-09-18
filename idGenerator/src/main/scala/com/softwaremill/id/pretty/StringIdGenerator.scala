package com.softwaremill.id.pretty

trait StringIdGenerator {
  def nextId(): String

  def idBaseAt(timestamp: Long): String
}
