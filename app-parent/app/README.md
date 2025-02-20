set these env vars to run locally from IDE

    PLAYGROUND_API_REST_DATA_GENERATOR_ENABLED=true # if you want to enable sample test data generation endpoints
    
    PLAYGROUND_API_REST_SANDBOX_ENABLED=true # if you want to enable sample webflux endpoints
    
    SPRING_BOOT_ADMIN_CLIENT_INSTANCE_SERVICE_URL=http://host.docker.internal:8088 # when your SBA is run by a win docker desktop.

sba and pg are assumed running locally in docker.

when running locally in a dev environment, the root compose ships all needed components:

- PG database
- SBA
- KC as authorization server
  - where you'd have to create 
    - a realm
      - set its corresponding urls in your "app" auth(spring.security.oauth2.resourceserver) & authz(spring.security.oauth2.authorizationserver) & springdoc(springdoc.swagger-ui.oauth) conf urls, through env vars, whether run from IDE or via compose
        - by looking into the KC endpoint http://{KC}/realms/{your-realm-name}-realm/.well-known/openid-configuration 
    - your test users
    - clients
      - one for swagger-ui; this one should be:
        - public, non confidential
        - intended for: authorization code flow (W/PKCE)
        - intended for "human" clients of our api willing to grant it authorization to act on their behalf
      - one for the app; this one should be:
        - confidential
        - intended for: client credentials flow
        - intended for "machine" clients of our api. can be tested later on by postman
    - roles and scopes: as defined & required by @PreAuthorize access control rules
    - assign appropriate roles to users, directly, or through groups. it's up to you.
    - assign appropriate scopes to (confidential) clients
- Prometheus
- Grafana