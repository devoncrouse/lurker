package com.mechhome.lurker

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.stream.scaladsl.Source
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.io.IO
import akka.actor.Terminated
import akka.util.ByteString
import akka.stream.OverflowStrategy
import java.nio.ByteBuffer
import com.fazecast.jSerialComm._
import com.mechhome.lurker.Window.Coordinates
import com.mechhome.lurker.ClientActor.SetLabel

object MotorActor {
  case class Stop()
  case class ChannelRange(min: Int, max: Int)
  def apply(port: String) = Props(classOf[MotorActor], port)
}

class MotorActor(port: String) extends Actor with ActorLogging {
  import MotorActor._
  import context._

  //val clientAddress = "akka://Lurker/user/client"
  val clientAddress = "akka.tcp://Lurker@10.10.10.110:2552/user/client"

  val sp = SerialPort.getCommPort(port)
  val ranges = Map(1 -> new ChannelRange(1, 127), 2 -> new ChannelRange(255, 129))

  override def preStart() = {
    log.info(s"Requesting manager to open port: ${port}")
    sp.setBaudRate(19200)
    sp.setNumStopBits(1)
    sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, 1000)
    sp.openPort()

    while (!sp.isOpen()) {
      Thread.sleep(1000L)
      log.info("Connecting...")
    }

    context become opened
    log.info("Connected")
  }

  // Char  Function
  // 0     Shuts Down Channel 1 and 2
  // 1     Channel 1 - Full Reverse
  // 64    Channel 1 - Stop
  // 127   Channel 1 - Full Forward
  // 128   Channel 2 - Full Reverse
  // 192   Channel 2 - Stop
  // 255   Channel 2 - Full Forward
  def coordinatesToCommands(c: Coordinates) = {
    val reverse = if (c.y < 0) -1 else 1
    val turn = Maths.normalize(c.x, -127, 127, -20, 20) * reverse
    val m1r = ranges.get(1).get
    val m2r = ranges.get(2).get
    val m1y = Maths.normalize(c.y, -127, 127, m1r.min, m1r.max) + turn
    val m2y = Maths.normalize(c.y, -127, 127, m2r.min, m2r.max) + turn
    sp.writeBytes(Array(m1y.byteValue(), m2y.byteValue()), 2)
    context.actorSelection(clientAddress) ! SetLabel("x: " + c.x + " y: " + c.y + " m1: " + m1y + " m2: " + m2y)
  }

  def receive: Receive = {
    case _ => log.warning(sender().path.toStringWithoutAddress + ": unprepared for message")
  }

  def opened: Receive = {
    case Stop           => sp.writeBytes(Array(0), 1)
    case c: Coordinates => coordinatesToCommands(c)
    case _              => log.warning(sender().path.toStringWithoutAddress + ": unrecognized message")
  }
}