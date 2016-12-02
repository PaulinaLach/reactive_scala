package com.lab6.actors

import akka.actor.{Actor, ActorRef, ActorRefFactory}

class Seller(private val auctionTitles: Array[String], auctionMaker: ActorRefFactory => ActorRef) extends Actor {
  override def preStart(): Unit = {
    for (title <- auctionTitles) {
      println(s"Creating auction: $title")
      val auction = auctionMaker(context)
      auction ! InitializeAuction(title)
      context.actorSelection("/user/auctionSearch") ! SubscribeToSearch(title, auction)
    }
  }

  override def receive: Receive = {
    case SuccessfulBid(amount) =>
      println(s"Success $amount")
    case WinAuction(amount, i) =>
      println(s"Won auction with $amount")
  }
}

object Seller {
  case class Register(subscribeToSearch: SubscribeToSearch)
  case class Unregister(subscribeToSearch: SubscribeToSearch)
}
