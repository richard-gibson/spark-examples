package examples.streaming.actor

import akka.actor._
import examples.streaming.actor.Publisher.{UnSubscribe, Subscribe}
import org.apache.spark.streaming.receiver.ActorHelper

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/**
 * User: Richard
 * Date: 29/12/2014
 * Time: 20:26
 */
class SimpleConsumer[T: ClassTag](urlOfPublisher: String)
  extends Actor with ActorHelper {

  private val remotePublisher = context.actorSelection(urlOfPublisher)

  override def preStart() = identifyEcho()

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
    case msg => store(msg.asInstanceOf[T])
  }

  def becomeRunning(echo: ActorRef): Unit = {
    log.info("Running")
    context setReceiveTimeout Duration.Undefined
    context watch echo
    context become running(echo)
  }

  def identifyEcho(): Unit = {
    remotePublisher ! Identify(0)
  }

  override def postStop() = remotePublisher ! UnSubscribe(context.self)

}

object SimpleConsumer {
  def props[T: ClassTag](urlOfPublisher: String): Props =
    Props(new SimpleConsumer[T](urlOfPublisher))
}

