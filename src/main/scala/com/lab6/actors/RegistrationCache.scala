package com.lab6.actors

case class RegistrationCache(expectedResp: Int) {
  val registerResponses: ResponseCache = ResponseCache()
  val unregisterResponses: ResponseCache = ResponseCache()

  def scheduleCallbackOnRegisterActionResponse(auction: SubscribeToSearch, onRespAcquired: SubscribeToSearch => Unit): Unit = {
    registerResponses.scheduleCallbackOnResponse(auction, expectedResp, onRespAcquired)
  }

  def registerActionResponse(auction: SubscribeToSearch): Unit = registerResponses.acknowledge(auction)

  def scheduleCallbackOnUnregisterResponse(auction: SubscribeToSearch, onRespAcquired: SubscribeToSearch => Unit): Unit = {
    unregisterResponses.scheduleCallbackOnResponse(auction, expectedResp, onRespAcquired)
  }

  def unregisterActionResponse(auction: SubscribeToSearch): Unit = unregisterResponses.acknowledge(auction)
}

case class ResponseCache() {
  var responses: Map[SubscribeToSearch, ResponseItem] = Map()

  def scheduleCallbackOnResponse(auction: SubscribeToSearch, expectedResp: Int, onRespAcquired: SubscribeToSearch => Unit): Unit = {
    responses += (auction -> ResponseItem(expectedResp, 0, onRespAcquired))
  }

  def acknowledge(auction: SubscribeToSearch): Unit = {
    val maybeItem: Option[ResponseItem] = responses.get(auction)
    maybeItem foreach increaseRespCount(auction)
  }

  def increaseRespCount(subscribeToSearch: SubscribeToSearch)(item: ResponseItem): Unit = {
    if (item.actualResp + 1 == item.expectedResp) {
      responses -= subscribeToSearch
      item.onRespAcquired(subscribeToSearch)
    } else {
      responses += (subscribeToSearch -> ResponseItem(item.expectedResp, item.actualResp + 1, item.onRespAcquired))
    }
  }
}

case class ResponseItem(expectedResp: Int, actualResp: Int, onRespAcquired: SubscribeToSearch => Unit)
