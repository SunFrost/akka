package se.scalablesolutions.akka.actor

import java.util.concurrent.{CountDownLatch, TimeUnit}
import org.scalatest.junit.JUnitSuite
import org.junit.Test
import Actor._

import se.scalablesolutions.akka.dispatch.Dispatchers

object ReactorBasedSingleThreadEventDrivenDispatcherActorSpec {
  class TestActor extends Actor {
    self.dispatcher = Dispatchers.newReactorBasedSingleThreadEventDrivenDispatcher(self.uuid)

    def receive = {
      case "Hello" =>
        self.reply("World")
      case "Failure" =>
        throw new RuntimeException("expected")
    }
  }

  object OneWayTestActor {
    val oneWay = new CountDownLatch(1)
  }
  class OneWayTestActor extends Actor {
    self.dispatcher = Dispatchers.newExecutorBasedEventDrivenDispatcher(self.uuid)
    def receive = {
      case "OneWay" => OneWayTestActor.oneWay.countDown
    }
  }
}

class ReactorBasedSingleThreadEventDrivenDispatcherActorSpec extends JUnitSuite {
  import ReactorBasedSingleThreadEventDrivenDispatcherActorSpec._

  private val unit = TimeUnit.MILLISECONDS

  @Test def shouldSendOneWay = {
    val actor = actorOf[OneWayTestActor].start
    val result = actor ! "OneWay"
    assert(OneWayTestActor.oneWay.await(1, TimeUnit.SECONDS))
    actor.stop
  }

  @Test def shouldSendReplySync = {
    val actor = actorOf[TestActor].start
    val result: String = (actor !! ("Hello", 10000)).get
    assert("World" === result)
    actor.stop
  }

  @Test def shouldSendReplyAsync = {
    val actor = actorOf[TestActor].start
    val result = actor !! "Hello"
    assert("World" === result.get.asInstanceOf[String])
    actor.stop
  }

  @Test def shouldSendReceiveException = {
    val actor = actorOf[TestActor].start
    try {
      actor !! "Failure"
      fail("Should have thrown an exception")
    } catch {
      case e =>
        assert("expected" === e.getMessage())
    }
    actor.stop
  }
}
