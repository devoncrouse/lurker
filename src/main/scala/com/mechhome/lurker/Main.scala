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
      val portDefault = "/dev/tty.usbserial-A6025ZVA"
      val motorDefault = "akka.tcp://Lurker@10.10.10.58:2552/user/motors"

      args(1) match {
        case "client" =>
          val clientActor = system.actorOf(ClientActor(args(2) ?? motorDefault), name = "client")
          clientActor ! ClientActor.ShowWindow
        case "driver" =>
          val motorActor = system.actorOf(MotorActor(args(2) ?? portDefault), name = "motors")
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


