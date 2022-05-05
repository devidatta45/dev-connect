package com.jobandtalent.clients

import sttp.client3.SttpBackend
import zio.Task

trait ZioSttpBackendWrapper[F[_], +G] {
  val backend: Task[SttpBackend[F, G]]
}
