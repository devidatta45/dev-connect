package com.jobandtalent.clients

import cats.implicits._
import com.jobandtalent.caching.ApplicationCaching
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
      cachedResult <- ZIO.accessM[ApplicationCaching](_.applicationCaching.getOrganisations(username))
      finalResponse <- if (cachedResult.isEmpty) {
        for {
          response <- request.send(backend).mapError(error => GithubError(error.getMessage))
          result <- response.body match {
            case Right(rawValue) => serializeToGithubRepoResponseAndUpdateCache(username, rawValue)
            case Left(errorResponse) => ZIO.fromEither {
              ValidationErrors(Vector(ErrorResponse("Github", errorResponse, response.code.toString()))).asLeft.asRight
            }
          }
        } yield result
      } else {
        ZIO.fromEither(cachedResult.asRight.asRight)
      }
    } yield finalResponse
  }

  def serializeToGithubRepoResponseAndUpdateCache(username: String,
                                                  rawValue: String): ZIO[ApplicationCaching, DomainError, Either[ValidationErrors, Vector[GithubResponse]]] = {
    for {
      serializedValue <- ZIO.fromEither(Either.catchNonFatal {
        parse(rawValue).extract[Vector[GithubResponse]]
      }.leftMap(error => SerializationError(error, "Github_Organisation")))
      _ <- ZIO.accessM[ApplicationCaching](_.applicationCaching.saveOrganisations(username, serializedValue))
    } yield serializedValue.asRight
  }
}

object LiveZioGithubClient extends LiveZioGithubClient