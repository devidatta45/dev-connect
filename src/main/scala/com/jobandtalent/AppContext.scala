package com.jobandtalent

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.jobandtalent.caching.{ApplicationCaching, InMemoryApplicationCaching}
import com.jobandtalent.clients._
import com.jobandtalent.routes.DevConnectRoutes

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

trait AppContext extends Directives {

  implicit def executionContext: ExecutionContext = system.dispatcher

  implicit def system: ActorSystem

  implicit def timeout: Timeout = Duration.fromNanos(100000)

  lazy val config = system.settings.config

  // Live environment for the application with all required dependency
  object LiveEnvironment extends GithubClient with TwitterClient with ApplicationCaching {
    override val githubService: GithubClient.Service = LiveZioGithubClient.githubService
    override val twitterService: TwitterClient.Service = LiveZioTwitterClient.twitterService
    override val applicationCaching: ApplicationCaching.Service = InMemoryApplicationCaching.applicationCaching
  }

  lazy val routes: Route = new DevConnectRoutes(LiveEnvironment).routes
}
