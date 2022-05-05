package com.jobandtalent.clients

import com.jobandtalent.models.{DomainError, GithubResponse, ValidationErrors}
import zio._

trait GithubClient {
  val githubService: GithubClient.Service
}

object GithubClient {
  trait Service {
    def getOrganisations(username: String): ZIO[GithubClient, DomainError, Either[ValidationErrors, Vector[GithubResponse]]]
  }
}