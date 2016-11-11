package com.lab2first

import akka.actor.ActorSystem
import akka.actor.FSM.StateTimeout
import akka.testkit.{TestActorRef, TestFSMRef, TestKit, TestProbe}
import com.lab2first.actors._
import scala.concurrent.duration._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSpecLike}

class BuyerSpec extends TestKit(ActorSystem("BuyerSpec")) with FunSpecLike with BeforeAndAfterAll
  with BeforeAndAfterEach{

  override def afterAll(): Unit = {
    system.terminate
  }

  describe("An Buyer") {
    describe("when NotificateBided") {
      it("Bid sender more if money available") {
        val auctionSender = TestProbe()
        val buyer = TestActorRef(new Buyer(100.0f))
        auctionSender.send(buyer, SearchResponse(Seq()))

        auctionSender.send(buyer, NotificateBided(90.0f))
        auctionSender.expectMsg(Bid(91.0f))
      }

      it("do not bid sender more if not more money") {
        val auctionSender = TestProbe()
        val buyer = TestActorRef(new Buyer(50.0f))
        auctionSender.send(buyer, SearchResponse(Seq()))

        auctionSender.send(buyer, NotificateBided(90.0f))
        auctionSender.expectNoMsg(500.millisecond)
      }
    }
  }
}
