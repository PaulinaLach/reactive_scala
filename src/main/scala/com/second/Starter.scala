package com.second

import akka.actor.{ActorSystem, Props}
import com.first.actors.Auction

/**
 * This is actually just a small wrapper around the generic launcher
 * class akka.Main, which expects only one argument: the class name of
 * the application?s main actor. This main method will then create the
 * infrastructure needed for running the actors, start the given main
 * actor and arrange for the whole application to shut down once the main
 * actor terminates.
 *
 * Thus you could also run the application with a
 * command similar to the following:
 * java -classpath  akka.Main com.first.actors.HelloWorldActor
 *
 * @author alias
 */
object Starter {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem()

    val auctions = for (i <- 1 to 5) yield system.actorOf(Props(classOf[Auction], i), "Auction" + i)
    val buyers = for (i <- 1 to 5) yield system.actorOf(Props(classOf[actors.Buyer], auctions), "Buyer" + i)
  }

}
