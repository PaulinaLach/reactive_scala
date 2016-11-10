package com.lab2first.actors

import akka.actor.{Actor, ActorRef}

case class SubscribeToSearch(title: String)
case class SearchAuction(value: String)
case class SearchResponse(results: Seq[ActorRef])

class AuctionSearch extends Actor {
  private val auctions = new scala.collection.mutable.HashMap[String, ActorRef]

  def receive(): Receive = {
    case SubscribeToSearch(title) =>
      println(s"Auction $title added to searcher.")
      auctions += (title -> sender)
    case SearchAuction(value) =>
      sender ! SearchResponse(auctions.filter(_._1.contains(value)).values.toList)
  }
}
