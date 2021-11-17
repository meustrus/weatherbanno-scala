package com.example.weatherbanno

import com.example.weatherbanno.OpenWeatherApi.{OneCallResponse, OneCallResponseCurrentConditions, OneCallResponseWeather}
import io.circe.jawn.decode
import munit.CatsEffectSuite

class OpenWeatherApiSpec extends CatsEffectSuite {
  test("real API response decodes") {
    val realAPIResponse = "{" +
      "\"lat\":90,\"lon\":-41," +
      "\"timezone\":\"Etc/GMT+3\",\"timezone_offset\":-10800," +
      "\"current\":{\"dt\":1637180084,\"temp\":250.97,\"feels_like\":243.97,\"pressure\":1022,\"humidity\":100,\"dew_point\":250.97,\"uvi\":0,\"clouds\":95,\"visibility\":5534,\"wind_speed\":2.49,\"wind_deg\":194,\"wind_gust\":2.41,\"weather\":[{\"id\":804,\"main\":\"Clouds\",\"description\":\"overcast clouds\",\"icon\":\"04n\"}]}" +
      "}"

    val decodedAPIResponse = decode[OneCallResponse](realAPIResponse)

    assertEquals(decodedAPIResponse, Right(OneCallResponse(
      OneCallResponseCurrentConditions(1637180084, 243.97f, Seq(OneCallResponseWeather("Clouds"))),
      Seq()
    )))
  }
}
