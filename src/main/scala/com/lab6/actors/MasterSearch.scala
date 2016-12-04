package com.lab6.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.{ActorRefRoutee, Broadcast, Router, RoutingLogic}
import com.lab6.actors.AuctionSearch.{Registered, Unregistered}
import com.lab6.actors.Seller.{Register, Unregister}

class MasterSearch(numberOfRoutes: Int, dispatchingStrategy: RoutingLogic, routeFactory: () => ActorRef) extends Actor {
  val ackCache: RegistrationCache = RegistrationCache(numberOfRoutes)
  val router = createRouterWith(routesNumber = numberOfRoutes, routingStrategy = dispatchingStrategy)

  private def createRouterWith(routesNumber: Int, routingStrategy: RoutingLogic): Router = {
    val routes = Vector.fill(routesNumber)(singleRoute)
    Router(routingStrategy, routes)
  }

  private def singleRoute: ActorRefRoutee = {
    val route = routeFactory()
    ActorRefRoutee(route)
  }

  def scheduleRegisteredAcknowledgmentFor(auction: SubscribeToSearch, seller: ActorRef) = {
    ackCache.scheduleCallbackOnRegisterActionResponse(auction, auction => seller ! Registered(auction))
  }

  def scheduleUnregisteredAcknowledgmentFor(auction: SubscribeToSearch, seller: ActorRef) = {
    ackCache.scheduleCallbackOnUnregisterResponse(auction, auction => seller ! Unregistered(auction))
  }

  override def receive: Receive = LoggingReceive {
    case msg@Register(auction) =>
      scheduleRegisteredAcknowledgmentFor(auction, sender())
      router.route(Broadcast(msg), self)

    case msg@Unregister(auction) =>
      scheduleUnregisteredAcknowledgmentFor(auction, sender())
      router.route(Broadcast(msg), self)

    case msg: SearchAuction =>
      router.route(msg, sender())

    case Registered(auction) =>
      ackCache.registerActionResponse(auction)

    case Unregistered(auction) =>
      ackCache.unregisterActionResponse(auction)
  }
}

object MasterSearch {
  def props(numberOfRoutes: Int, dispatchingStrategy: RoutingLogic, routeFactory: () => ActorRef): Props =
    Props(new MasterSearch(numberOfRoutes, dispatchingStrategy, routeFactory))
}
