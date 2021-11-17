package com.example.weatherbanno

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object WeatherbannoRoutes {
  def weatherRoutes[F[_]: Sync](w: Weather[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "weather" / "lat" / lat / "lon" / lon =>
        for {
          coords <- Weather.Coords.fromStrings[F](lon, lat)
          summary <- w.summary(coords)
          resp <- Ok(summary)
        } yield resp
      case GET -> Root / "weather" / "lon" / lon / "lat" / lat =>
        for {
          coords <- Weather.Coords.fromStrings[F](lon, lat)
          summary <- w.summary(coords)
          resp <- Ok(summary)
        } yield resp
    }
  }
}
