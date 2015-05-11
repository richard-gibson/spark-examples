/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
