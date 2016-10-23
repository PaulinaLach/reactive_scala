package com.lab2first.actors

import akka.actor.Actor
import akka.actor.Actor.Receive

class Seller(private val auctionTitles: Array[String]) extends Actor {
  override def receive: Receive = {
    case SuccessfulBid(amount) =>
      println(s"Success $amount")
  }
}
