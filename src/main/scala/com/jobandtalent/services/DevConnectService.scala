package com.jobandtalent.services

import com.jobandtalent.clients.{GithubClient, TwitterClient}
import com.jobandtalent.models._
import zio.ZIO
import cats.implicits._

import scala.annotation.tailrec

trait DevConnectService {

  def areConnected(dev1: String, dev2: String): ZIO[GithubClient with TwitterClient, DomainError, Either[ValidationErrors, ConnectedResponse]]
}

object DevConnectService {
  val service = new DevConnectService {
    override def areConnected(dev1: String, dev2: String): ZIO[GithubClient with TwitterClient,
      DomainError, Either[ValidationErrors, ConnectedResponse]] = {
      for {
        dev1Organisations <- ZIO.accessM[GithubClient](_.githubService.getOrganisations(dev1))
        dev2Organisations <- ZIO.accessM[GithubClient](_.githubService.getOrganisations(dev2))
        dev1TwitterDetails <- ZIO.accessM[TwitterClient](_.twitterService.getUserDetails(dev1))
        dev2TwitterDetails <- ZIO.accessM[TwitterClient](_.twitterService.getUserDetails(dev2))
        githubValidationErrors = concatFailedEither(Vector(dev1Organisations, dev2Organisations))
        twitterValidationErrors = concatFailedEither(Vector(dev1TwitterDetails, dev2TwitterDetails))
        connectedResponse <- if (githubValidationErrors.errors.nonEmpty || twitterValidationErrors.errors.nonEmpty) {
          ZIO.fromEither {
            val errorList: Either[DomainError, Either[ValidationErrors, ConnectedResponse]] = {
              (githubValidationErrors ++ twitterValidationErrors).asLeft.asRight
            }
            errorList
          }
        } else {
          findRelation(dev1, dev2, dev1Organisations, dev2Organisations, dev1TwitterDetails, dev2TwitterDetails)
        }
      } yield connectedResponse
    }

    def findRelation(dev1: String,
                     dev2: String,
                     dev1Organisations: Either[ValidationErrors, Vector[GithubResponse]],
                     dev2Organisations: Either[ValidationErrors, Vector[GithubResponse]],
                     dev1TwitterDetails: Either[ValidationErrors, TwitterResponse],
                     dev2TwitterDetails: Either[ValidationErrors, TwitterResponse],
                    ): ZIO[GithubClient with TwitterClient, DomainError, Either[ValidationErrors, ConnectedResponse]] = {
      for {
        dev1Organisations <- ZIO.fromEither(dev1Organisations).mapError(validationError => GithubError(validationError.errors.mkString(",")))
        dev2Organisations <- ZIO.fromEither(dev2Organisations).mapError(validationError => GithubError(validationError.errors.mkString(",")))
        dev1TwitterDetails <- ZIO.fromEither(dev1TwitterDetails).mapError(validationError => TwitterError(validationError.errors.mkString(",")))
        dev2TwitterDetails <- ZIO.fromEither(dev2TwitterDetails).mapError(validationError => TwitterError(validationError.errors.mkString(",")))
        dev1Followers <- ZIO.accessM[TwitterClient](_.twitterService.getFollowers(dev1TwitterDetails.data.id))
        dev2Followers <- ZIO.accessM[TwitterClient](_.twitterService.getFollowers(dev2TwitterDetails.data.id))
        commonOrganisations = dev1Organisations.map(_.login).intersect(dev2Organisations.map(_.login))
        twitterValidationErrors = concatFailedEither(Vector(dev1Followers, dev2Followers))
        finalResponse <- if (twitterValidationErrors.errors.nonEmpty) {
          ZIO.fromEither {
            val errorList: Either[DomainError, Either[ValidationErrors, ConnectedResponse]] = {
              twitterValidationErrors.asLeft.asRight
            }
            errorList
          }
        } else {
          for {
            dev1Followers <- ZIO.fromEither(dev1Followers).mapError(validationError => TwitterError(validationError.errors.mkString(",")))
            dev2Followers <- ZIO.fromEither(dev2Followers).mapError(validationError => TwitterError(validationError.errors.mkString(",")))
            connected = dev1Followers.data.map(_.username).contains(dev2) &&
              dev2Followers.data.map(_.username).contains(dev1) &&
              commonOrganisations.nonEmpty
            connectedResponse = if (connected) ConnectedResponse(connected, Some(commonOrganisations)) else ConnectedResponse(connected, None)
          } yield connectedResponse.asRight
        }
      } yield finalResponse

    }
  }


  @tailrec
  private def concatFailedEither[T](
                                     eithers: Vector[Either[ValidationErrors, T]],
                                     errorList: Vector[ErrorResponse] = Vector.empty
                                   ): ValidationErrors = {
    if (eithers.isEmpty) {
      ValidationErrors(errorList)
    } else {
      eithers.head match {
        case Left(validationErrors) => concatFailedEither(eithers.tail, errorList ++ validationErrors.errors)
        case Right(_) => concatFailedEither(eithers.tail, errorList)
      }
    }

  }

}