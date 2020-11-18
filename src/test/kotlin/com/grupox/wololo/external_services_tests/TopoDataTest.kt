package com.grupox.wololo.external_services_tests

import arrow.core.extensions.either.foldable.get
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.Coordinates
import com.grupox.wololo.model.externalservices.GeoRef
import com.grupox.wololo.model.externalservices.HttpService
import com.grupox.wololo.model.externalservices.TopoData
import com.grupox.wololo.model.helpers.getOrThrow
import io.github.rybalkinsd.kohttp.dsl.http
import okhttp3.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Spy
import org.springframework.boot.test.mock.mockito.SpyBean

class TopoDataTest {
    private val topoData: TopoData = TopoData()
    private val coordinate1 = Coordinates((-62.435146).toFloat(), (-37.771053).toFloat())
    private val coordinate2 = Coordinates((-62.418774).toFloat(), (-36.89055).toFloat())

    private val correctJsonResponse: String = "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"elevation\": 311.2, \n" +
            "      \"location\": {\n" +
            "        \"lat\": -37.771053, \n" +
            "        \"lng\": -62.435146\n" +
            "      }\n" +
            "    }, \n" +
            "    {\n" +
            "      \"elevation\": 130.5, \n" +
            "      \"location\": {\n" +
            "        \"lat\": -36.89055, \n" +
            "        \"lng\": -62.418774\n" +
            "      }\n" +
            "    }\n" +
            "  ], \n" +
            "  \"status\": \"OK\"\n" +
            "}"
    private val incorrectJsonResponse: String = ""
    private lateinit var response: Response

    @Test
    fun `successfully request elevation data from some Coordinates returns the elevation for each coordinate`() {
        response = Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), correctJsonResponse))
                .code(200)
                .message("")
                .request(Request.Builder().url("https://api.opentopodata.org/v1/test-dataset?locations=-37.771053,-62.435146|-36.89055,-62.418774").build())
                .protocol(Protocol.HTTP_2)
                .build()

        topoData.httpService = spy(HttpService("TopoData"))
        Mockito.doReturn(response).`when`(topoData.httpService).getData("https://api.opentopodata.org/v1/test-dataset?locations=-37.771053,-62.435146|-36.89055,-62.418774")

        val result = topoData.requestElevation(listOf(coordinate1, coordinate2)).getOrThrow()
        assertThat(result.size).isEqualTo(2)
        assertThat(result.map { it.elevation }.sorted()).isEqualTo(listOf(130.5, 311.2))
        assertTrue(coordinate2.isEqualTo(result.minBy { it.elevation }!!.location))
        assertTrue(coordinate1.isEqualTo(result.maxBy { it.elevation }!!.location))
    }
/*
    @Test
    fun `request town data with status code 500 as response throws UnsuccessfulExternalRequestException`() {
        response = Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), correctJsonResponse))
                .code(500)
                .message("")
                .request(Request.Builder().url("https://apis.datos.gob.ar/georef/api/departamentos").build())
                .protocol(Protocol.HTTP_2)
                .build()

        geoRef = Mockito.spy(GeoRef())
        Mockito.doReturn(response).`when`(geoRef).postDataToGeoRef("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo"))

        assertThrows<CustomException.Service.UnsuccessfulExternalRequestException> {
            geoRef.requestTownsData("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo")).getOrThrow()
        }
    }

    @Test
    fun `request town data with body == null as response throws InvalidExternalResponseException`() {
        response = Response.Builder()
                .body(null)
                .code(200)
                .message("")
                .request(Request.Builder().url("https://apis.datos.gob.ar/georef/api/departamentos").build())
                .protocol(Protocol.HTTP_2)
                .build()

        geoRef = Mockito.spy(GeoRef())
        Mockito.doReturn(response).`when`(geoRef).postDataToGeoRef("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo"))

        assertThrows<CustomException.Service.InvalidExternalResponseException> {
            geoRef.requestTownsData("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo")).getOrThrow()
        }
    }

    @Test
    fun `request town data with any result == empty as response throws InvalidExternalResponseException`() {
        response = Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), incorrectJsonResponse))
                .code(200)
                .message("")
                .request(Request.Builder().url("https://apis.datos.gob.ar/georef/api/departamentos").build())
                .protocol(Protocol.HTTP_2)
                .build()

        geoRef = Mockito.spy(GeoRef())
        Mockito.doReturn(response).`when`(geoRef).postDataToGeoRef("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo"))

        assertThrows<CustomException.Service.InvalidExternalResponseException> {
            geoRef.requestTownsData("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo")).getOrThrow()
        }
    }*/
}