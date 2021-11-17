package com.example.weatherbanno

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

import scala.concurrent.duration.DurationInt

object WeatherbannoServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      apiKey <- Stream.eval(
        Async[F].catchNonFatal(sys.env("OPENWEATHERMAP_API_KEY"))
      )
      httpClient <- Stream.resource(
        EmberClientBuilder.default[F].withTimeout(5.seconds).build
      )
      openWeatherApi = OpenWeatherApi.impl[F](httpClient, apiKey)
      weatherAlg = Weather.impl[F](openWeatherApi)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        WeatherbannoRoutes.weatherRoutes[F](weatherAlg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
