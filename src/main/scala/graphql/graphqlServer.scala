package graphql

/**
 * Created by niuzhaojie on 23/10/16.
 */

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import sangria.parser.QueryParser
import sangria.execution.{ErrorWithResolver, QueryAnalysisError, Executor}
import sangria.marshalling.sprayJson._

import spray.json._

import scala.util.{Success, Failure}

import graphql.Schema._


object graphsqlServer extends App {
  implicit val system = ActorSystem("graph-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val dataActor = system.actorOf(Props[TestActor])

  //val dataRepo = new ActorRepo(dataActor)
  //val human = dataRepo.getHuman("1000")
  //print(human)

  val route: Route =
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson ⇒
        val JsObject(fields) = requestJson

        val JsString(query) = fields("query")

        val operation = fields.get("operationName") collect {
          case JsString(op) ⇒ op
        }

        val vars = fields.get("variables") match {
          case Some(obj: JsObject) ⇒ obj
          case Some(JsString(s)) if s.trim.nonEmpty ⇒ s.parseJson
          case _ ⇒ JsObject.empty
        }

        QueryParser.parse(query) match {

          // query parsed successfully, time to execute it!
          case Success(queryAst) ⇒
            complete(Executor.execute(SchemaDefinition.StarWarsSchema, queryAst, new ActorRepo(dataActor),
              variables = vars,
              operationName = operation,
              deferredResolver = new ActorFriendsResolver(dataActor))
              .map(OK → _)
              .recover {
              case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
              case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
            })

          // can't parse GraphQL query, return error
          case Failure(error) ⇒
            complete(BadRequest, JsObject("error" -> JsString(error.getMessage)))
        }
      }
    }

  Http().bindAndHandle(route, "0.0.0.0", sys.props.get("http.port").fold(8080)(_.toInt))
}
