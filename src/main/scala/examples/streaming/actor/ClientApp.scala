package examples.streaming.actor

import akka.actor._
import examples.streaming.actor.Publisher._

import scala.concurrent.duration._
import scala.io.StdIn

/**
 * User: Richard
 * Date: 26/12/2014
 * Time: 23:46
 *
 * Test ClientApp to consume from examples.streaming.actor.PublisherApp
 */
object ClientApp extends App {

  System.setProperty("akka.remote.netty.tcp.port", "0") // Use a random port for the client
  val hostname = "localhost"

  implicit val system = ActorSystem("demo-client-system")

  class Act(url:String) extends Actor with ActorLogging {
    val remotePublisher = context.actorSelection(url)

    log.info("Initializing")
    context setReceiveTimeout(5 seconds)
    identifyEcho()
    def receive = initializing

    def initializing :Receive = {
        case ActorIdentity(0, Some(ackRef)) =>
          ackRef ! Subscribe(self)
          becomeRunning(ackRef)
        case ReceiveTimeout => identifyEcho()
    }

    def running(echo: ActorRef) :Receive = {
      case Terminated(`echo`) =>
        context become initializing
      case message =>
        println(message.toString)
    }

    def becomeRunning(echo: ActorRef): Unit = {
      log.info("Running")
      context setReceiveTimeout Duration.Undefined
      context watch echo
      context become running(echo)
    }

    def identifyEcho(): Unit =  {
      println("identifyEcho")
      remotePublisher ! Identify(0)
    }
  }

  system.actorOf(
    Props(classOf[Act], s"akka.tcp://publisher-system@$hostname:2552/user/publisher-actor"), "SampleReceiver"
  )

  StdIn.readLine(f"Hit ENTER to exit ...%n")
  system.shutdown()
  system.awaitTermination()
}
