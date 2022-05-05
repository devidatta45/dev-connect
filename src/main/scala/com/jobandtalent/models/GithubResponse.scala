package com.jobandtalent.models

final case class GithubResponse(login: String)

final case class TwitterResponse(data: DataResponse)

final case class TwitterFollowerResponse(data: Vector[DataResponse])

final case class DataResponse(id: String, username: String)

final case class ConnectedResponse(connected: Boolean, organisations: Option[Vector[String]])

final case class ErrorResponse(
                                source: String,
                                message: String,
                                code: String
                              )

final case class ValidationErrors(errors: Vector[ErrorResponse]) {
  def ++(validationErrors: ValidationErrors): ValidationErrors = ValidationErrors(errors ++ validationErrors.errors)
}