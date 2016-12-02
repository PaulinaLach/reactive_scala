package com.lab6.notifications

import akka.actor._
import com.lab6.notifications.AuctionPublisher.NotificationReceived
import com.lab6.notifications.Notifier.{Notification, NotificationPayload}
import com.lab6.notifications.NotifierRequest.SuccessDeliver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class NotifierRequest(notificationToSend: Notification) extends Actor with ActorLogging {
  override def receive: Receive = {
    case NotificationReceived => notifyParent()
    case exception: Throwable => throw exception
  }

  override def preStart(): Unit = {
    sendNotification()
  }

  def sendNotification(): Unit = {
    val actorSelection: ActorSelection = notificationToSend.target
    val eventualActorRef: Future[ActorRef] = actorSelection.resolveOne(10 seconds)
    eventualActorRef.onSuccess(sendNotification)
    eventualActorRef.onFailure(throwException)

    def sendNotification: PartialFunction[ActorRef, Unit] = {
      case ref: ActorRef => ref ! notificationToSend.payload
    }

    def throwException: PartialFunction[Throwable, Unit] = {
      case ex: Throwable => self ! ex
    }
  }

  def notifyParent(): Unit = {
    context.parent ! SuccessDeliver(notificationToSend.payload)
    context.stop(self)
  }
}

object NotifierRequest {
  def props(notificationToSend: Notification): Props = Props(new NotifierRequest(notificationToSend))
  case class SuccessDeliver(payload: NotificationPayload)
}
