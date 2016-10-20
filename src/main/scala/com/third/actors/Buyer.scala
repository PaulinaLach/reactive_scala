package com.third.actors

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

import scala.util.Random

case object Start


class Buyer(var money: Float, val auctions: Vector[ActorRef]) extends Actor {
  def receive: Receive = LoggingReceive {
    case Start =>
      println("Starting buyer")
      auctions.foreach((auction) => {
        auction ! Bid(Random.nextInt(1000) + 10)
      })
      context become bidding
  }

  def bidding: Receive = LoggingReceive {
    case SuccessfulBid(amount) =>
      money -= amount
      println("Bid successfully")
    case FailedBid =>
      println("Bid failure: ")
    case WinAuction(amount, i) =>
      println(s"$self Won Auction$i with $amount")
  }
}
