package com.mechhome.lurker

import scala.swing.MainFrame
import scala.swing.Button
import java.awt.Dimension
import scala.swing.Label
import scala.swing.event.MouseClicked
import scala.swing.event.KeyTyped
import scala.swing.Component
import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.Swing
import akka.actor.ActorRef
import scala.swing.event.MouseMoved
import java.awt.geom.Line2D
import java.awt.Color
import java.awt.Graphics2D
import com.mechhome.lurker.Window.Coordinates
import org.slf4j.LoggerFactory
import akka.actor.ActorContext

object Window {
  case class Coordinates(x: Integer, y: Integer)
}

class Window(context: ActorContext, motorAddress: String) extends MainFrame {
  val log = LoggerFactory.getLogger(this.getClass.getName)
  val deadZone = 20
  val resultLabel = new Label("Hi.")
  val canvas = new Canvas()
  title = "Lurker"
  preferredSize = new Dimension(480, 640)

  contents = new BoxPanel(Orientation.Vertical) {
    contents += resultLabel
    contents += Swing.VStrut(10)
    contents += canvas
    border = Swing.EmptyBorder(10, 10, 10, 10)
  }

  private def mouseClick(x: Int, y: Int) {
    val c = normalize(x, y)
    resultLabel.text = "Click! " + c.x + ":" + c.y
    resultLabel.repaint()
    context.actorSelection(motorAddress) ! MotorActor.Stop
  }

  private def mouseMove(x: Int, y: Int) {
    val c = normalize(x, y)
    resultLabel.text = c.x + ":" + c.y
    resultLabel.repaint()
    context.actorSelection(motorAddress) ! c
  }

  private def keyType(c: Char) {
  }

  private def normalize(x: Int, y: Int): Coordinates = {
    var normX = Maths.normalize(x, 0, canvas.size.width, -127, 127)
    var normY = Maths.normalize(y, 0, canvas.size.height, 127, -127)
    if (math.abs(normX) < deadZone) normX = 0
    if (math.abs(normY) < deadZone) normY = 0
    return Coordinates(normX, normY)
  }

  class Canvas extends Component {
    preferredSize = new Dimension(480, 640)
    focusable = true
    listenTo(mouse.clicks)
    listenTo(mouse.moves)
    listenTo(keys)

    override def paintComponent(g: Graphics2D) {
      g.setColor(Color.white);
      g.fillRect(0, 0, size.width, size.height);

      // Draw crosshairs
      g.setColor(Color.black);
      val yMid = size.height / 2
      val xMid = size.width / 2
      g.drawLine(0, yMid, size.width, yMid)
      g.drawLine(xMid, 0, xMid, size.height)

      // Draw box around deadzone
      val deadWidth = Maths.normalize(deadZone, 0, 127, 0, size.width)
      val deadHeight = Maths.normalize(deadZone, 0, 127, 0, size.height)
      g.setColor(Color.lightGray)
      g.setBackground(Color.red)
      g.drawRect(xMid - (deadWidth / 2), yMid - (deadHeight / 2), deadWidth, deadHeight)
      repaint()
    }

    reactions += {
      case MouseMoved(_, p, _)         => mouseMove(p.x, p.y)
      case MouseClicked(_, p, _, _, _) => mouseClick(p.x, p.y)
      case KeyTyped(_, c, _, _)        => keyType(c)
    }
  }
}