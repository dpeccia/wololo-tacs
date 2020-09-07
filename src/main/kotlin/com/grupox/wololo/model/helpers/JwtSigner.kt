package com.grupox.wololo.model.helpers

import arrow.core.*
import arrow.core.extensions.either.applicativeError.handleError
import arrow.core.extensions.either.applicativeError.raiseError
import com.grupox.wololo.errors.CustomException
import io.jsonwebtoken.*
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
                .setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(1))))
                .setIssuedAt(Date.from(Instant.now()))
                .compact()
    }

    fun validateJwt(jwt: Option<String>): Either<CustomException, Jws<Claims>> =
            try {
                Right(Jwts.parserBuilder()
                        .setSigningKey(keyPair.public)
                        .build()
                        .parseClaimsJws(jwt.getOrElse { throw CustomException.TokenException("Token not found. Login Again") }))
            } catch (ex: ExpiredJwtException) { Left(CustomException.TokenException("Token expired. Login Again"))
            } catch (ex: CustomException) { Left(ex) }
}