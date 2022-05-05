package com.jobandtalent.utils

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import com.jobandtalent.models.{DomainError, GithubError, SerializationError, TwitterError}

object DomainErrorMapper extends Directives with JsonSupport {
  val domainErrorMapper: ErrorMapper[DomainError] = {
    case GithubError(message, code) =>
      HttpResponse(StatusCodes.InternalServerError, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(code, message)))

    case error: SerializationError =>
      HttpResponse(StatusCodes.BadRequest, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(error.code, error.message)))

    case TwitterError(message, code) =>
      HttpResponse(StatusCodes.InternalServerError, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(code, message)))
  }

  case class GenericErrorResponseBody(code: String, message: String, errorDetails: Option[String] = None)

}
