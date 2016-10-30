package com.lab2first.actors

import akka.actor.{Actor, ActorRef}

case object Subscribe
case class SearchAuction(value: String)

class AuctionSearch extends Actor {
//  val auctions: List[ActorRef] = new Array[ActorRef](0)
//
  def receive(): Receive = {
    case Subscribe =>
    case SearchAuction(value) =>
      sender ! value
  }

}
