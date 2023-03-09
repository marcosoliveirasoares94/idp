package br.gov.sp.prodesp.idp;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.theme.ThemeSelectorProvider;
import org.keycloak.theme.ThemeSelectorProviderFactory;

public class ThemeProviderFactory implements ThemeSelectorProviderFactory {

    @Override
    public ThemeSelectorProvider create( KeycloakSession session ) {
        return new IdpSPProvider( session );
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
        return "IdpSPThemeSelector";
    }
}