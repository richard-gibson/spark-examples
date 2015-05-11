package examples.streaming.actor

import akka.actor._
import scala.io.StdIn
import scala.util.Random

import scala.concurrent.duration._
import Publisher._

/**
 * User: Richard
 * Date: 27/12/2014
 * Time: 17:58
 */
object Publisher {
   sealed trait FeederMsg
   object tick extends FeederMsg
   case class Subscribe(receiverActor: ActorRef) extends FeederMsg
   case class UnSubscribe(receiverActor: ActorRef) extends FeederMsg

   def props = Props(new Publisher)

 }

/**
  * Sends the random content to every receiver subscribed
  **/
class Publisher extends Actor with ActorLogging{
  val strings = Vector("first", "second", "third", "fourth")
  val rand = new Random()

  def makeMessage: String = {
    val x = rand.nextInt(4)
    s"${strings(x)} ${strings(3 - x)}"
  }

  def receive: Receive = broadcast(List.empty)

  def broadcast(consumers: List[ActorRef]): Receive = {
    case Subscribe(consumer: ActorRef) =>
      log.info(s"received subscribe from $consumer")
      context become broadcast(consumer :: consumers)

    case UnSubscribe(consumer: ActorRef) =>
      log.info(s"received unsubscribe from $consumer")
      context become broadcast(consumers filter (_ eq consumer))

    case tick =>
      log.debug(s"consumers ${consumers.length}")
      consumers.foreach(_ ! makeMessage)
  }
 }

object PublisherApp extends App {

  implicit val system = ActorSystem("publisher-system")
  import system.dispatcher

  val publisher = system.actorOf(Publisher.props, "publisher-actor")
  system.scheduler.schedule(50 milli,  100 milli, publisher, tick)

  StdIn.readLine(f"Hit ENTER to exit ...%n")
  system.shutdown()
  system.awaitTermination()
}