package com.vetclinic.utility;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vetclinic.models.Utente;
import org.springframework.beans.factory.annotation.Value;


public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;
    private Utente utente;

    public static String getNameFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim("preferred_username").asString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim("sub").asString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String generateToken(Utente utente) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(utente.getFirstName())
                .withClaim("preferred_username", utente.getEmail())
                .sign(algorithm);
    }


}
