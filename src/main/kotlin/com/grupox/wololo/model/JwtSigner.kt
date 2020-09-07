package com.grupox.wololo.model

import arrow.core.*
import com.grupox.wololo.errors.CustomException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.security.KeyPair
import java.time.Duration
import java.time.Instant
import java.util.Date

@Service
object JwtSigner {
    private val keyPair: KeyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)

    fun createJwt(userId: String): String {
        return Jwts.builder()
                .signWith(keyPair.private, SignatureAlgorithm.RS256)
                .setSubject(userId)
                .setIssuer("identity")
                .setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(60))))
                .setIssuedAt(Date.from(Instant.now()))
                .compact()
    }

    fun validateJwt(jwt: String): Either<CustomException, Jws<Claims>> {
        return try {
            Right(Jwts.parserBuilder().setSigningKey(keyPair.public).build().parseClaimsJws(jwt))
        } catch (ex : Exception) {
            Left(CustomException.ExpiredTokenException())
        }
    }
}