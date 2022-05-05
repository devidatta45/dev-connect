package com.jobandtalent.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import com.jobandtalent.caching.ApplicationCaching
import com.jobandtalent.clients.{GithubClient, TwitterClient}
import com.jobandtalent.models.{DomainError, GithubResponse}
import com.jobandtalent.services.DevConnectService
import com.jobandtalent.utils.{DomainErrorMapper, ErrorMapper, JsonSupport, ZioToRoutes}
import zio.internal.Platform

import scala.concurrent.ExecutionContext

class DevConnectRoutes(env: GithubClient with TwitterClient with ApplicationCaching)(
  implicit executionContext: ExecutionContext,
  system: ActorSystem,
) extends ZioToRoutes[GithubClient with TwitterClient with ApplicationCaching] with Directives with JsonSupport {

  override def environment: GithubClient with TwitterClient with ApplicationCaching = env

  override def platform: Platform = Platform.default

  private lazy val service = DevConnectService.service

  implicit val errorMapper: ErrorMapper[DomainError] = DomainErrorMapper.domainErrorMapper

  val routes = pathPrefix("developers" / "connected" / Segment / Segment) { (dev1, dev2) =>
    get {
      for {
        connectedResponse <- service.areConnected(dev1, dev2)
        finalResponse = connectedResponse match {
          case Right(response) => complete(
            StatusCodes.OK,
            response
          )
          case Left(validationErrors) => complete(
            StatusCodes.BadRequest,
            validationErrors
          )
        }
      } yield finalResponse
    }
  }
}
