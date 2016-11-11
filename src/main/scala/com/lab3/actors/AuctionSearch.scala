package com.lab3.actors

import akka.actor.{Actor, ActorRef}

case class SubscribeToSearch(title: String)
case class SearchAuction(value: String)
case class SearchResponse(results: Seq[ActorRef])

class AuctionSearch extends Actor {
  val auctions = new scala.collection.mutable.HashMap[String, ActorRef]

  def receive(): Receive = {
    case SubscribeToSearch(title) =>
      println(s"Auction $title added to searcher.")
      auctions += (title -> sender)
    case SearchAuction(value) =>
      println(s"Searching for $value")
      println(s"Data in search $auctions")
      val searchResults = auctions.filter(_._1.contains(value)).values.toList
      sender ! SearchResponse(searchResults)
      println(s"Returned $searchResults")
  }


}
