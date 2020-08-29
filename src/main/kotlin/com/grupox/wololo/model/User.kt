package com.grupox.wololo.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class User (var id: Int, @JsonProperty var nombre: String,
            @JsonProperty var mail: String,
            @JsonProperty var password: String,
            @JsonProperty val esAdmin: Boolean){
}