package com.lab2first.actors

import akka.actor.{Actor, ActorSystem, Props}

class Seller(private val auctionTitles: Array[String]) extends Actor {

  override def preStart(): Unit = {
    val system = ActorSystem()
    for (title <- auctionTitles) {
      println(s"Creating auction: $title")
      system.actorOf(Props(classOf[Auction], self), title) ! InitializeAuction
    }
  }

  override def receive: Receive = {
    case SuccessfulBid(amount) =>
      println(s"Success $amount")
    case WinAuction(amount, i) =>
      println(s"Won auction with $amount")
  }
}
