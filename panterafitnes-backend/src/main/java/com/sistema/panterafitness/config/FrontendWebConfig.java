package com.sistema.panterafitness.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Sirve el frontend compilado (Next.js "static export") desde el mismo servidor que la API.
 *
 * <p>Next genera un archivo HTML por ruta (por ejemplo {@code admin/clases.html}). Este resolver
 * traduce la URL limpia {@code /admin/clases} a ese archivo. Las rutas {@code /api/**} nunca se
 * resuelven aqui: si un endpoint no existe responde 404 de la API y no una pagina HTML.
 *
 * <p>Si el JAR se construye sin frontend (desarrollo con {@code npm run dev} en el puerto 3000),
 * no hay archivos estaticos y esta configuracion no tiene efecto.
 */
@Configuration
public class FrontendWebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // JS y CSS de Next llevan un hash en el nombre: nunca cambian, el navegador los guarda un año.
    registry
        .addResourceHandler("/_next/static/**")
        .addResourceLocations("classpath:/static/_next/static/")
        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
    // Imágenes del sitio (logo, ícono): un día de caché.
    registry
        .addResourceHandler("/brand/**", "/favicon.ico", "/icon.png", "/apple-icon.png")
        .addResourceLocations("classpath:/static/brand/", "classpath:/static/")
        .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());
    registry
        .addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(new NextStaticExportResolver());
  }

  static class NextStaticExportResolver extends PathResourceResolver {

    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
      if (resourcePath.startsWith("api/") || resourcePath.equals("api")) {
        return null;
      }
      String path = resourcePath.endsWith("/")
          ? resourcePath.substring(0, resourcePath.length() - 1)
          : resourcePath;
      if (path.isEmpty()) {
        return readable(location.createRelative("index.html"));
      }
      if (hasExtension(path)) {
        return super.getResource(path, location);
      }
      Resource html = readable(location.createRelative(path + ".html"));
      if (html != null) {
        return html;
      }
      // Ruta inexistente del frontend: pagina 404 generada por Next.
      return readable(location.createRelative("404.html"));
    }

    private static boolean hasExtension(String path) {
      int slash = path.lastIndexOf('/');
      return path.indexOf('.', slash + 1) >= 0;
    }

    private static Resource readable(Resource resource) {
      return resource.exists() && resource.isReadable() ? resource : null;
    }
  }
}
