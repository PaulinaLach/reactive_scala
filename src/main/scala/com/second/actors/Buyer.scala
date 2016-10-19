package com.second.actors

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

class Buyer(val auctions: Vector[ActorRef]) extends Actor {

  override def preStart(): Unit = {
    super.preStart()
    auctions.foreach(_ ! Bid(scala.util.Random.nextInt(100)+10))
  }

  def receive = LoggingReceive {
    case Done => println("success")
    case Failed => println("failed")
  }
}
