FROM quay.io/keycloak/keycloak:22.0.3 as builder

ENV KC_DB=postgres

WORKDIR /opt/keycloak

RUN keytool -genkeypair \
    -storepass password \
    -storetype PKCS12 \
    -keyalg RSA \
    -keysize 2048 \
    -dname "CN=server" \
    -alias server \
    -ext "SAN:c=DNS:localhost,IP:127.0.0.1" \
    -keystore conf/server.keystore

FROM quay.io/keycloak/keycloak:latest

COPY --from=builder /opt/keycloak/ /opt/keycloak/


ENTRYPOINT [ "/opt/keycloak/bin/kc.sh" ]
