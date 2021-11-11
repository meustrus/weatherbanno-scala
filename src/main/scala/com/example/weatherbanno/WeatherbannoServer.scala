package com.example.weatherbanno

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import com.snowplowanalytics.weather.providers.openweather.CreateOWM
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object WeatherbannoServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      client <- Stream.resource(CreateOWM[F].create(
        "api.openweathermap.org", sys.env("OPENWEATHER_API_KEY"), timeout = 1.seconds, ssl = true
      ))
      weatherAlg = Weather.impl[F](client)

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
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
        Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
