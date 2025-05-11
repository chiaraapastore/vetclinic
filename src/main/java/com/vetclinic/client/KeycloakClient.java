package com.vetclinic.client;


import com.vetclinic.models.RoleKeycloak;
import com.vetclinic.models.TokenRequest;
import com.vetclinic.models.UtenteKeycloak;
import jakarta.validation.constraints.NotBlank;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name = "keycloakClient", url = "${keycloak.admin.auth-server-url}")
public interface KeycloakClient {

    @RequestMapping(method = RequestMethod.POST,
            value = "/realms/${keycloak.admin.realm}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Object> getAccessToken(@RequestBody TokenRequest tokenRequest);

    @RequestMapping(method = RequestMethod.POST,
            value = "/admin/realms/${keycloak.admin.realm}/users",
            produces = "application/json")
    ResponseEntity<Object> createUsers(@RequestHeader("Authorization") String accessToken, @RequestBody UtenteKeycloak utenteKeycloak);

    @RequestMapping(method = RequestMethod.GET,
            value = "/admin/realms/${keycloak.admin.realm}/ui-ext/available-roles/users/{id}",
            produces = "application/json")
    ResponseEntity<List<RoleKeycloak>> getAvailableRoles(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("id") String id,
            @RequestParam(value = "first", defaultValue = "0") String first,
            @RequestParam(value = "max", defaultValue = "100") String max,
            @RequestParam(value = "search", defaultValue = "") String search);

    @RequestMapping(method = RequestMethod.POST,
            value = "/admin/realms/${keycloak.admin.realm}/users/{id}/role-mappings/realm",
            produces = "application/json")
    ResponseEntity<Object> addRoleToUser(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("id") String id,
            @RequestBody List<RoleRepresentation> roles);

    @GetMapping("/admin/realms/${keycloak.admin.realm}/roles")
    ResponseEntity<List<RoleKeycloak>> getRealmRoles(@RequestHeader("Authorization") String accessToken);

    @PostMapping("/admin/realms/${keycloak.admin.realm}/users/{id}/role-mappings/realm")
    ResponseEntity<Object> addRealmRoleToUser(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("id") String userId,
            @RequestBody List<RoleRepresentation> roles);


    @GetMapping("/admin/realms/{realm}/users")
    ResponseEntity<List<UserRepresentation>> searchUserByUsername(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("realm") String realm,
            @RequestParam("username") String username
    );

    @DeleteMapping("/admin/realms/{realm}/users/{userId}")
    ResponseEntity<Object> deleteUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("realm") String realm,
            @PathVariable("userId") String userId
    );
}