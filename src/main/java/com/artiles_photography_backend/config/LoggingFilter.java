package com.artiles_photography_backend.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.artiles_photography_backend.models.HttpLog;
import com.artiles_photography_backend.repository.HttpLogRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author arojas
 */
@Component
public class LoggingFilter implements jakarta.servlet.Filter {

	private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

	@Autowired
	private HttpLogRepository httpLogRepository;

	@Override
	public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Envolver la solicitud y la respuesta para capturar el contenido
		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

		// Continuar con la cadena de filtros
		chain.doFilter(requestWrapper, responseWrapper);

		// Obtener información de la solicitud
		String requestURI = httpRequest.getRequestURI();
		String method = httpRequest.getMethod();
		String queryString = httpRequest.getQueryString();
		String requestBody = getRequestBody(requestWrapper);

		// Obtener información de la respuesta
		int status = responseWrapper.getStatus();
		String responseBody = getResponseBody(responseWrapper);
		String contentType = responseWrapper.getContentType();

		// Crear entidad HttpLog
		HttpLog httpLog = new HttpLog(
				method,
				requestURI,
				queryString,
				requestBody,
				status,
				contentType,
				responseBody);

		// Guardar en la base de datos
		try {
			httpLogRepository.save(httpLog);
		} catch (Exception e) {
			logger.error("Error al guardar el log HTTP en la base de datos: {}", e.getMessage());
		}

		// Registrar solo un resumen en el archivo de log
		logger.info("HTTP Request: {} {}?{} | Status: {}", method, requestURI,
				queryString != null ? queryString : "", status);

		// Copiar el contenido de la respuesta al flujo original
		responseWrapper.copyBodyToResponse();
	}

	private String getRequestBody(ContentCachingRequestWrapper request) {
		byte[] content = request.getContentAsByteArray();
		if (content.length == 0) {
			return "[empty]";
		}
		String body = new String(content, StandardCharsets.UTF_8).replaceAll("\\n", "");
		// Limitar el tamaño para evitar cuerpos demasiado grandes
		return body.length() > 1000 ? body.substring(0, 1000) + "..." : body;
	}

	private String getResponseBody(ContentCachingResponseWrapper response) {
		byte[] content = response.getContentAsByteArray();
		if (content.length == 0) {
			return "[empty]";
		}
		String body = new String(content, StandardCharsets.UTF_8).replaceAll("\\n", "");
		// Limitar el tamaño para evitar cuerpos demasiado grandes
		return body.length() > 1000 ? body.substring(0, 1000) + "..." : body;
	}

	@Override
	public void init(jakarta.servlet.FilterConfig filterConfig) throws ServletException {
		// Inicialización si es necesaria
	}

	@Override
	public void destroy() {
		// Limpieza si es necesaria
	}
}