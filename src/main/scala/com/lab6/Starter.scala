package com.lab6

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import com.lab6.actors.{Auction, AuctionSearch, Buyer}
import com.lab6.notifications.{AuctionPublisher, Notifier}
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Random

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
    val config: Config = ConfigFactory.load()
    val auctionPublisherSystem: ActorSystem = ActorSystem("auction-publisher", config.getConfig("auction-publisher").withFallback(config))
    auctionPublisherSystem.actorOf(Props[AuctionPublisher], "auction-publisher")

    val system = ActorSystem()

    system.actorOf(Props[AuctionSearch], "auctionSearch")

    val sellers = for (i <- 1 to 5) yield system.actorOf(
      Props(classOf[actors.Seller], Array("Auction" + i), (f: ActorRefFactory) => {
        
        val notifier: ActorRef = f.actorOf(Props(classOf[Notifier], () => f.actorSelection("akka.tcp://auction-publisher@127.0.0.1:2553/user/auction-publisher")))
        f.actorOf(Props(classOf[Auction], "Auction" + i, notifier))
      })
    )
    val buyers = for (i <- 1 to 10)
      yield system.actorOf(Props(classOf[Buyer], Random.nextFloat() + Random.nextInt(1000) + 1000), "Buyer" + i)

    Thread sleep 6000
  }

}
