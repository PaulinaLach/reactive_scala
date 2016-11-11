package com.lab4.actors

import akka.actor.{Actor, ActorRef, ActorRefFactory}

class Seller(private val auctionTitles: Array[String], auctionMaker: ActorRefFactory => ActorRef) extends Actor {

  override def preStart(): Unit = {
    for (title <- auctionTitles) {
      println(s"Creating auction: $title")
      auctionMaker(context) ! InitializeAuction(title)
    }
  }

  override def receive: Receive = {
    case SuccessfulBid(amount) =>
      println(s"Success $amount")
    case WinAuction(amount, i) =>
      println(s"Won auction with $amount")
  }
}
