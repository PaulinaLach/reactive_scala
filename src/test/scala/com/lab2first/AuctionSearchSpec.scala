package com.lab2first

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.lab2first.actors.{AuctionSearch, SearchAuction, SearchResponse, SubscribeToSearch}
import org.scalatest.{BeforeAndAfterAll, FunSpec, FunSpecLike}

class AuctionSearchSpec extends TestKit(ActorSystem("AuctionSearchSpec")) with FunSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate
  }

  describe("A AuctionSearch") {
    describe("on received Subscribe") {
      it("add sender to list") {
        val proxy = TestProbe()
        val actor = TestActorRef(new AuctionSearch())
        proxy.send(actor, SubscribeToSearch("test"))

        assert(actor.underlyingActor.auctions.contains("test"))
        assert(actor.underlyingActor.auctions.get("test") == Option(proxy.ref))
      }
    }

    describe("on received SearchAuction") {
      it("returns found list to sender") {
        val proxy = TestProbe()
        val actor = TestActorRef(new AuctionSearch());
        actor.underlyingActor.auctions += ("test" -> proxy.ref)

        proxy.send(actor, SearchAuction("test"))

        proxy.expectMsg(SearchResponse(Seq(proxy.ref)))
      }
    }
  }
}
