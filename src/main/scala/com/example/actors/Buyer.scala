package com.example.actors

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.event.LoggingReceive

class Buyer(val auctions: List[Auction]) extends Actor {

  override def preStart(): Unit = {
    super.preStart()

  }

  def receive = LoggingReceive {
    case Done => println("success")
    case Failed => println("failed")
  }

  def bid(auction: Auction, amount: Float): Unit = {
    auction ! amount
  }
}