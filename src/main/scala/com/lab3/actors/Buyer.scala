package com.lab3.actors

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.Random

case class NotificateBided(amount: Float)
case class AuctionName(name: String)

class Buyer(var money: Float) extends Actor {
  private val eventualMasterSearch: Future[ActorRef] = context.actorSelection("/user/auctionSearch").resolveOne(1.second)
  eventualMasterSearch onSuccess {
    case masterSearch =>
      val title = "Auction" + (Random.nextInt(5 - 1) + 1)
      println(s"Searching for auction: $title")
      context.system.scheduler.scheduleOnce(1.second, masterSearch, SearchAuction(title))
  }

  def receive: Receive = LoggingReceive {
    case SearchResponse(results: Seq[ActorRef]) =>
      println("Starting buyer")
      results.foreach((auction) => {
        auction ! Bid(Random.nextInt(1000) + 10)
      })
      context.become(bidding)
  }

  def bidding: Receive = LoggingReceive {
    case SuccessfulBid(amount) =>
      money -= amount
      println("Bid successfully")
    case FailedBid =>
      println("Bid failure: ")
    case WinAuction(amount, i) =>
      println(s"$self Won Auction$i with $amount")
    case NotificateBided(amount) if amount < money =>
      println(s"Someone else has bid auction with $amount")
      sender ! Bid(amount + 1)
  }
}
