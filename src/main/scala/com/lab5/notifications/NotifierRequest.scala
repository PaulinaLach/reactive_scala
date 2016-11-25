package com.lab5.notifications

import akka.actor._
import com.lab5.notifications.AuctionPublisher.NotificationReceived
import com.lab5.notifications.Notifier.{Notification, NotificationPayload}
import com.lab5.notifications.NotifierRequest.SuccessDeliver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class NotifierRequest(notificationToSend: Notification) extends Actor with ActorLogging {

  override def receive: Receive = {
    case NotificationReceived => notifyParentAboutSuccessfulDeliveryAndKillYourself()
    case exception: Throwable => throw exception
  }

  override def preStart(): Unit = {
    tryToSendNotification()
  }

  def tryToSendNotification(): Unit = {
    val actorSelection: ActorSelection = notificationToSend.target
    val eventualActorRef: Future[ActorRef] = actorSelection.resolveOne(10 seconds)
    eventualActorRef.onSuccess(sendNotification)
    eventualActorRef.onFailure(rethrowException)

    def sendNotification: PartialFunction[ActorRef, Unit] = {
      case ref: ActorRef => ref ! notificationToSend.payload
    }

    def rethrowException: PartialFunction[Throwable, Unit] = {
      case ex: Throwable => self ! ex
    }
  }

  def notifyParentAboutSuccessfulDeliveryAndKillYourself(): Unit = {
    context.parent ! SuccessDeliver(notificationToSend.payload)
    context.stop(self)
  }
}

object NotifierRequest {
  def props(notificationToSend: Notification): Props = Props(new NotifierRequest(notificationToSend))
  case class SuccessDeliver(payload: NotificationPayload)
}
