import sbt._

object Version {
  val spark     = "1.2.0"
  val slf4j     = "1.7.6"
  val logback   = "1.1.1"
  val mockito   = "1.9.5"
  val akka      = "2.3.3"
}

object Library {
  // workaround until 2.11 version for Spark Streaming's available
  val sparkStreaming        = "org.apache.spark"  %% "spark-streaming" % Version.spark
  val sparkStreamingTwitter = "org.apache.spark"  %% "spark-streaming-twitter" % Version.spark
  val akkaActor             = "com.typesafe.akka" %% "akka-actor"      % Version.akka
  val akkaTestKit           = "com.typesafe.akka" %% "akka-testkit"    % Version.akka
  val slf4jApi              = "org.slf4j"         %  "slf4j-api"       % Version.slf4j
  val logbackClassic        = "ch.qos.logback"    %  "logback-classic" % Version.logback
}

object Dependencies {

  import Library._

  val sparkExampleDeps = Seq(
    sparkStreaming,
    logbackClassic
  )
}