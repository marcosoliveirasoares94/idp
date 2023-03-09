package br.gov.sp.prodesp.idp;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class EventProviderFactory implements EventListenerProviderFactory {

    @Override
    public EventListenerProvider create( KeycloakSession keycloakSession ) {
        return new IdpSPProvider( keycloakSession );
    }

    @Override
    public void init( Config.Scope config ) {
    }

    @Override
    public void postInit( KeycloakSessionFactory factory ) {
    }

    @Override
    public void close( ) {
    }

    @Override
    public String getId( ) {
        return "IdpSPEventSelector";
    }
}