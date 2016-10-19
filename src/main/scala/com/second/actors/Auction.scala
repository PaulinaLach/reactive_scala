package com.second.actors

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration

case class Bid(amount: Float)
case object Relist
case object BidTimer
case object DeleteTimer
case object InitializeAuction
case object Done
case object Failed

class Auction(val i: Int) extends Actor {
  val buyers: Vector[ActorRef]
  var timer: Cancellable
  var bidValue = 0.0

  def receive: Receive = LoggingReceive {
    case InitializeAuction =>
      rescheduleOnce(10 seconds, BidTimer)
      context become created
  }

  def created: Receive = LoggingReceive {
    case BidTimer =>
      rescheduleOnce(20 seconds, DeleteTimer)
      context become ignored

    case Bid(amount) if bidValue < amount =>
      println(s"auction $i - previous amount: $bidValue new amount: $amount")
      bidValue = amount
      context become activated
  }

  def ignored: Receive = LoggingReceive {
    case Relist =>
      rescheduleOnce(10 seconds, BidTimer)
      context become created
    case DeleteTimer =>
    //      self.delete!
  }

  def activated: Receive = LoggingReceive {
    case Bid(amount) if bidValue < amount =>
      println(s"auction $i - previous amount: $bidValue new amount: $amount")
      bidValue = amount
      sender ! Done
    case BidTimer =>
      rescheduleOnce(20 seconds, DeleteTimer)
      context become sold
    case _ => sender ! Failed
  }

  def sold: Receive = LoggingReceive {
    case DeleteTimer =>
    //      self.delete!
  }

  private def rescheduleOnce(value: FiniteDuration, caseObject: Object) = {
    if (timer != null) timer.cancel()
    timer = context.system.scheduler.scheduleOnce(value, self, caseObject)
  }
}
