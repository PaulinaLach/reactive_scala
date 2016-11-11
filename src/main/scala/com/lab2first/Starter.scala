package com.lab2first

import akka.actor.{ActorRefFactory, ActorSystem, Props}
import com.lab2first.actors._

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

    system.actorOf(Props[AuctionSearch], "auctionSearch")

    val sellers = for (i <- 1 to 5) yield system.actorOf(
      Props(classOf[Seller], Array("Auction" + i), (f: ActorRefFactory) => f.actorOf(Props[Auction])))
    )
    val buyers = for (i <- 1 to 5) yield system.actorOf(Props(classOf[Buyer], 0.0f), "Buyer" + i)

    Thread sleep 6000
  }

}
