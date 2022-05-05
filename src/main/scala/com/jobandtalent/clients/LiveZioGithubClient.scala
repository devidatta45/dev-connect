package com.jobandtalent.clients

import cats.implicits._
import com.jobandtalent.models.{DomainError, ErrorResponse, GithubError, GithubResponse, SerializationError, ValidationErrors}
import com.jobandtalent.utils.JsonSupport
import org.json4s.native.JsonMethods.parse
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.{Task, ZIO}

trait LiveZioGithubClient extends GithubClient with ZioSttpBackendWrapper[Task, ZioStreams] with JsonSupport {

  override val backend: Task[SttpBackend[Task, ZioStreams]] = HttpClientZioBackend()

  override val githubService: GithubClient.Service = (username: String) => {
    val request = basicRequest.get(uri"https://api.github.com/users/$username/orgs")

    for {
      backend <- backend.mapError(error => GithubError(error.getMessage))
      response <- request.send(backend).mapError(error => GithubError(error.getMessage))
      serializedResponse = response.body match {
        case Right(rawValue) => serializeToGithubRepoResponse(rawValue)
        case Left(errorResponse) => ValidationErrors(Vector(ErrorResponse("Github", errorResponse, response.code.toString()))).asLeft.asRight
      }
      finalResponse <- ZIO.fromEither(serializedResponse)
    } yield finalResponse
  }

  def serializeToGithubRepoResponse(rawValue: String): Either[DomainError, Either[ValidationErrors, Vector[GithubResponse]]] = {
    Either.catchNonFatal {
      parse(rawValue).extract[Vector[GithubResponse]].asRight
    }.leftMap(error => SerializationError(error, "Github_Organisation"))
  }
}

object LiveZioGithubClient extends LiveZioGithubClient