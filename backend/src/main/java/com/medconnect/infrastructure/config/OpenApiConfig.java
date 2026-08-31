package com.medconnect.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // El JWT viaja en una cookie httpOnly: no hay forma de "autorizar" desde
    // el boton de Swagger UI (ni JS ni Swagger pueden leer/setear una cookie
    // httpOnly). Para probar un endpoint protegido desde /swagger-ui.html hay
    // que estar logueado en la app en el mismo navegador/origen -- el
    // "Try it out" manda la cookie sola, como cualquier otro fetch del sitio.
    @Bean
    public OpenAPI medConnectOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MedConnect API")
                .description("Gestión de turnos e historias clínicas. La autenticación usa una cookie httpOnly "
                        + "(POST /api/auth/login) — logueate en la app desde este mismo navegador antes de "
                        + "probar un endpoint protegido con \"Try it out\".")
                .version("v1"));
    }
}
