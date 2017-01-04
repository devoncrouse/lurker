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
import akka.actor.ActorContext

object ClientActor {
  def apply(motorAddress: String) = Props(classOf[ClientActor], motorAddress)
  case class ShowWindow()
  case class SetLabel(label: String)
}

class ClientActor(motorAddress: String) extends Actor with ActorLogging {
  import ClientActor._
  import context._

  var client = new Window(context, motorAddress)

  def receive: Receive = {
    case ShowWindow => client.visible = true
    case SetLabel(l) => {
      client.resultLabel.text = l
      client.resultLabel.repaint()
    }

    case _ => log.warning(sender().path.toStringWithoutAddress + ": unprepared for message")
  }
}