package com.example.weatherbanno

import cats.effect.IO
import com.example.weatherbanno.OpenWeatherApi.{OneCallResponse, OneCallResponseAlert, OneCallResponseCurrentConditions, OneCallResponseWeather}
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite

import java.time.Instant

class WeatherSpec extends CatsEffectSuite {

  test("Weather returns status code 200") {
    assertIO(retWeather.map(_.status) ,Status.Ok)
  }

  test("Weather returns expected summary") {
    assertIO(retWeather.flatMap(_.as[String]), "{" +
      "\"timestamp\":\"2021-11-16T10:12:13Z\"," +
      "\"current_temperature_feel\":\"moderate\"," +
      "\"current_conditions\":[\"Thunderstorm\",\"Snow\"]," +
      "\"current_alerts\":[{\"sender_name\":\"SERN\",\"event\":\"elpsycongru\",\"description\":\"???\"}]" +
      "}")
  }

  private[this] val retWeather: IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/weather/lon/41/lat/-93")
    val owa = new OpenWeatherApi[IO] {
      override def oneCall(lon: Double, lat: Double): IO[OneCallResponse] = {
        assert(lon == 41)
        assert(lat == -93)
        IO.pure(OneCallResponse(
          OneCallResponseCurrentConditions(
            Instant.parse("2021-11-16T10:12:13Z").getEpochSecond,
            271,
            Seq(
              OneCallResponseWeather("Thunderstorm"),
              OneCallResponseWeather("Snow")
            )
          ),
          Seq(OneCallResponseAlert("SERN", "elpsycongru", "???"))
        ))
      }
    }
    val weather = Weather.impl[IO](owa)
    WeatherbannoRoutes.weatherRoutes(weather).orNotFound(getHW)
  }
}