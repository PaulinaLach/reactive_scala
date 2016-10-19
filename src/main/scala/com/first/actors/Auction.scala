package com.first.actors

import akka.actor.Actor
import akka.event.LoggingReceive

case class Bid(amount: Float)
case object Done
case object Failed

class Auction(val i: Int) extends Actor {
  var bidValue = 0.0

  def receive = LoggingReceive {
    case Bid(amount) if bidValue < amount =>
      println(s"auction $i - previous amount: $bidValue new amount: $amount")
      bidValue = amount
      sender ! Done
    case _ => sender ! Failed
  }
}
