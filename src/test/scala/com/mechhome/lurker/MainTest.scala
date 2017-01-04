package com.mechhome.lurker

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Before
import org.slf4j.LoggerFactory
import org.junit.Test
import org.junit.Ignore

class MainTest extends AssertionsForJUnit {
  val log = LoggerFactory.getLogger(this.getClass.getName)
  @Before
  def before() {
    log.info("Starting main tests...")
  }

  @Test
  @Ignore
  def testMain() {
    Main.main(Array(""))
  }
}