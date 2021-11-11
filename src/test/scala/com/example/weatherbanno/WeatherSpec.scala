package com.example.weatherbanno

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite

class WeatherSpec extends CatsEffectSuite {

  test("Weather returns status code 200") {
    assertIO(retWeather.map(_.status) ,Status.Ok)
  }

  test("Weather returns weather world message") {
    assertIO(retWeather.flatMap(_.as[String]), "{\"message\":\"Weather, world\"}")
  }

  private[this] val retWeather: IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/weather/world")
    val weather = Weather.impl[IO]
    WeatherbannoRoutes.weatherRoutes(weather).orNotFound(getHW)
  }
}