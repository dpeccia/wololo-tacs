package com.grupox.wololo.external_services_tests

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.externalservices.GeoRef
import com.grupox.wololo.model.helpers.getOrThrow
import okhttp3.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy

class GeoRefTest {
    lateinit var geoRef: GeoRef

    private val correctJsonResponse: String = "{\"resultados\":[{\"cantidad\":1,\"departamentos\":[{\"centroide\":{\"lat\":-34.9157089757004,\"lon\":-59.9584822196811},\"id\":\"06224\",\"nombre\":\"Chivilcoy\",\"provincia\":{\"id\":\"06\",\"nombre\":\"Buenos Aires\"}}],\"inicio\":0,\"parametros\":{\"max\":1,\"nombre\":\"chivilcoy\"},\"total\":1},{\"cantidad\":1,\"departamentos\":[{\"centroide\":{\"lat\":-35.5103702936319,\"lon\":-58.7694635013319},\"id\":\"06547\",\"nombre\":\"Monte\",\"provincia\":{\"id\":\"06\",\"nombre\":\"Buenos Aires\"}}],\"inicio\":0,\"parametros\":{\"max\":1,\"nombre\":\"monte\"},\"total\":5},{\"cantidad\":1,\"departamentos\":[{\"centroide\":{\"lat\":-35.6765966680226,\"lon\":-59.7037033020054},\"id\":\"06707\",\"nombre\":\"Saladillo\",\"provincia\":{\"id\":\"06\",\"nombre\":\"Buenos Aires\"}}],\"inicio\":0,\"parametros\":{\"max\":1,\"nombre\":\"saladillo\"},\"total\":1}]}"
    private val incorrectJsonResponse: String = "{\"resultados\":[{\"cantidad\":0,\"departamentos\":[],\"inicio\":0,\"parametros\":{\"max\":1,\"nombre\":\"chivilcoy\"},\"total\":1},{\"cantidad\":1,\"departamentos\":[{\"centroide\":{\"lat\":-35.5103702936319,\"lon\":-58.7694635013319},\"id\":\"06547\",\"nombre\":\"Monte\",\"provincia\":{\"id\":\"06\",\"nombre\":\"Buenos Aires\"}}],\"inicio\":0,\"parametros\":{\"max\":1,\"nombre\":\"monte\"},\"total\":5},{\"cantidad\":1,\"departamentos\":[{\"centroide\":{\"lat\":-35.6765966680226,\"lon\":-59.7037033020054},\"id\":\"06707\",\"nombre\":\"Saladillo\",\"provincia\":{\"id\":\"06\",\"nombre\":\"Buenos Aires\"}}],\"inicio\":0,\"parametros\":{\"max\":1,\"nombre\":\"saladillo\"},\"total\":1}]}"
    private lateinit var response: Response

    @Test
    fun `successfully request town data from Towns Chivilcoy, Monte and Saladillo gets all the coordinates`() {
        response = Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), correctJsonResponse))
                .code(200)
                .message("")
                .request(Request.Builder().url("https://apis.datos.gob.ar/georef/api/departamentos").build())
                .protocol(Protocol.HTTP_2)
                .build()

        geoRef = spy(GeoRef())
        doReturn(response).`when`(geoRef).postDataToGeoRef("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo"))

        val result = geoRef.requestTownsData("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo")).getOrThrow()
        assertThat(result.size).isEqualTo(3)
        assertThat(result.map { it.name }.sorted()).isEqualTo(listOf("Chivilcoy", "Monte", "Saladillo"))
        assertThat(result.sortedBy { it.coordinates.latitude }.map { it.name }).isEqualTo(listOf("Saladillo", "Monte", "Chivilcoy"))
    }

    @Test
    fun `request town data with status code 500 as response throws UnsuccessfulExternalRequestException`() {
        response = Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), correctJsonResponse))
                .code(500)
                .message("")
                .request(Request.Builder().url("https://apis.datos.gob.ar/georef/api/departamentos").build())
                .protocol(Protocol.HTTP_2)
                .build()

        geoRef = spy(GeoRef())
        doReturn(response).`when`(geoRef).postDataToGeoRef("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo"))

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

        geoRef = spy(GeoRef())
        doReturn(response).`when`(geoRef).postDataToGeoRef("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo"))

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

        geoRef = spy(GeoRef())
        doReturn(response).`when`(geoRef).postDataToGeoRef("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo"))

        assertThrows<CustomException.Service.InvalidExternalResponseException> {
            geoRef.requestTownsData("Buenos Aires", listOf("Chivilcoy", "Monte", "Saladillo")).getOrThrow()
        }
    }
}