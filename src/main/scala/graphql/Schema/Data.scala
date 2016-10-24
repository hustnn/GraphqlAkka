package graphql.Schema

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout

import sangria.schema.{DeferredResolver, Deferred}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Try


/**
 * Created by niuzhaojie on 23/10/16.
 */
object Episode extends Enumeration {
  val NEWHOPE, EMPIRE, JEDI = Value
}

trait Character {
  def id: String
  def name: Option[String]
  def friends: List[String]
  def appearsIn: List[Episode.Value]
}

case class Human(
                  id: String,
                  name: Option[String],
                  friends: List[String],
                  appearsIn: List[Episode.Value],
                  homePlanet: Option[String]) extends Character

case class Droid(
                  id: String,
                  name: Option[String],
                  friends: List[String],
                  appearsIn: List[Episode.Value],
                  primaryFunction: Option[String]) extends Character

/**
 * Instructs sangria to postpone the expansion of the friends list to the last responsible moment and then batch
 * all collected defers together.
 */
case class DeferFriends(friends: List[String]) extends Deferred[List[Option[Character]]]

case class getHumanData(id: String)
case class getDroidData(id: String)
case class getFriends(friends: List[String])

/**
 * Resolves the lists of friends collected during the query execution.
 * For this demonstration the implementation is pretty simplistic, but in real-world scenario you
 * probably want to batch all of the deferred values in one efficient fetch.
 */
class FriendsResolver extends DeferredResolver[Any] {
  override def resolve(deferred: Vector[Deferred[Any]], ctx: Any) = deferred map {
    case DeferFriends(friendIds) =>
      Future.fromTry(Try(
        friendIds map (id => CharacterRepo.humans.find(_.id == id) orElse CharacterRepo.droids.find(_.id == id))))
  }
}


class TestActor extends Actor {

  def receive: Receive = {
    case getHumanData(id) =>
      sender() ! CharacterRepo.humans.find(c => c.id == id)
    case getDroidData(id) =>
      sender() ! CharacterRepo.droids.find(c => c.id == id)
    case getFriends(friendIds) =>
      sender() ! (friendIds map (id => CharacterRepo.humans.find(_.id == id) orElse CharacterRepo.droids.find(_.id == id)))
  }

}

class ActorFriendsResolver (dataRetrieve: ActorRef) extends DeferredResolver[Any] {

  val dataActor = dataRetrieve
  implicit val timeout = Timeout(5 seconds)

  override def resolve(deferred: Vector[Deferred[Any]], ctx: Any) = deferred map {
    case DeferFriends(friendIds) =>
      val future = dataActor ? getFriends(friendIds)
      val result = Await.result(future, timeout.duration)
      val friends = result match {
        case None => null
        case friendList: List[_] => friendList.map(_ match {
          case Some(human: Human) => human.asInstanceOf[Human]
          case Some(droid: Droid) => droid.asInstanceOf[Droid]
        })
      }
      Future.fromTry(Try(
        friendIds map (id => friends.find(_.id == id))))
  }
}

class ActorRepo (dataRetrieve: ActorRef) {
  val dataActor: ActorRef = dataRetrieve

  implicit val timeout = Timeout(5 seconds)

  def getHuman(id: String): Option[Human] = {
    val future = dataActor ? getHumanData(id)
    val result = Await.result(future, timeout.duration)
    result match {
      case None => null
      case Some(value) => Some(value.asInstanceOf[Human])
    }

  }

  def getDroid(id: String): Option[Droid] = {
    val future = dataActor ? getDroidData(id)
    val result = Await.result(future, timeout.duration)
    result match {
      case None => null
      case Some(value) => Some(value.asInstanceOf[Droid])
    }
  }

  def getHero(id: String): Option[Character] =
    getHuman(id) orElse getDroid(id)
}


class CharacterRepo {
  import CharacterRepo._

  def getHero(episode: Option[Episode.Value]) =
    episode flatMap (_ => getHuman("1000")) getOrElse droids.last

  def getHuman(id: String): Option[Human] = humans.find(c => c.id == id)

  def getDroid(id: String): Option[Droid] = droids.find(c => c.id == id)
}

object CharacterRepo {
  val humans = List(
    Human(
      id = "1000",
      name = Some("Luke Skywalker"),
      friends = List("1002", "1003", "2000", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Tatooine")),
    Human(
      id = "1001",
      name = Some("Darth Vader"),
      friends = List("1004"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Tatooine")),
    Human(
      id = "1002",
      name = Some("Han Solo"),
      friends = List("1000", "1003", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = None),
    Human(
      id = "1003",
      name = Some("Leia Organa"),
      friends = List("1000", "1002", "2000", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Alderaan")),
    Human(
      id = "1004",
      name = Some("Wilhuff Tarkin"),
      friends = List("1001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = None)
  )

  val droids = List(
    Droid(
      id = "2000",
      name = Some("C-3PO"),
      friends = List("1000", "1002", "1003", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      primaryFunction = Some("Protocol")),
    Droid(
      id = "2001",
      name = Some("R2-D2"),
      friends = List("1000", "1002", "1003"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      primaryFunction = Some("Astromech"))
  )
}


