package com.mechhome.lurker

import java.util.Properties
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.stream.scaladsl.Source
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.OverflowStrategy
import akka.stream.ThrottleMode
import akka.actor.Props

import scala.swing._

object Main {
  val log = LoggerFactory.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit = {
    log.info("Starting Lurker...")
    System.setProperty("apple.laf.useScreenMenuBar", "true")

    try {
      implicit val system = ActorSystem("Lurker")
      implicit val materializer = ActorMaterializer()

      args(0) match {
        case "client" =>
          val motor = if (args.length >= 2) args(1) else "akka.tcp://Lurker@10.10.10.58:2552/user/motors"
          val clientActor = system.actorOf(ClientActor(motor), name = "client")
          clientActor ! ClientActor.ShowWindow
        case "driver" =>
          val port = if (args.length >= 2) args(1) else "/dev/tty.usbserial-A6025ZVA"
          val client = if (args.length >= 3) args(2) else "akka.tcp://Lurker@10.10.10.110:2552/user/client"
          val motorActor = system.actorOf(MotorActor(port, client), name = "motors")
      }

      system.registerOnTermination(println("Stopped Lurker"))
      Await.result(system.whenTerminated, Duration.Inf)
    } catch {
      case e: Exception => log.error("Error in main", e)
    }
  }

  implicit class NullCoalescent[A](a: A) {
    def ??(b: => A) = if (a != null) a else b
  }
}


