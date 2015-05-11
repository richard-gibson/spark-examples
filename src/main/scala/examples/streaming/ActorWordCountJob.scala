package examples.streaming

import examples.streaming.actor.SimpleConsumer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext.toPairDStreamFunctions
import org.apache.spark.streaming.{Seconds, StreamingContext}


/**
 * A sample word count program adapted from Spark Streaming sample
 * from Remote Actor
 * Actor source can be started using examples.streaming.actor.PublisherApp
 *
 * Current consumer config hardcoded,
 *
 */
object ActorWordCountJob extends App{

  //move to config
  System.setProperty("akka.remote.netty.tcp.port", "0") // Use a random port for the client
  val host = "localhost"
  val port = 7777
  val batchWindowDuration = Seconds(2)
  val sparkConf = new SparkConf(false)
      .setAppName("ActorWordCount")
      .setMaster("local[*]") // run locally with enough threads
    .set("spark.logConf", "false")
    .set("spark.driver.port", s"$port")
    .set("spark.driver.host", s"$host")
    .set("spark.akka.logLifecycleEvents", "false")

    // Create the context and set the batch window duration
    val ssc = new StreamingContext(sparkConf, batchWindowDuration)

    val lines = ssc.actorStream[String](
      SimpleConsumer.props[String](s"akka.tcp://publisher-system@$host:2552/user/publisher-actor"),
      "SimpleConsumer")


    lines.flatMap(_.split("\\s"))
          .map((_,1))
          .reduceByKey(_ + _)
          .print()

    ssc.start()
    ssc.awaitTermination()

}
