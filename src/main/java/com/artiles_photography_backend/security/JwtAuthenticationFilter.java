package com.artiles_photography_backend.security;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.artiles_photography_backend.repository.JwtBlacklistRepository;
import com.artiles_photography_backend.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author arojas
 *
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final Map<String, Long> authCache = new HashMap<>();

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService,
            JwtBlacklistRepository jwtBlacklistRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.jwtBlacklistRepository = jwtBlacklistRepository;
    }

    private String calculateTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error al calcular el hash del token: {}", e.getMessage(), e);
            throw new RuntimeException("Error al calcular el hash del token", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String method = request.getMethod();
        String path = request.getRequestURI();
        logger.debug("Procesando solicitud en JwtAuthenticationFilter: {} {}", method, path);

        // Omitir procesamiento para solicitudes OPTIONS
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("Solicitud OPTIONS detectada, omitiendo autenticación JWT");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", authHeader != null ? authHeader : "No presente");

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.debug("Autenticación ya presente, continuando con la cadena de filtros");
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No se encontró token JWT en la solicitud");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String tokenHash = calculateTokenHash(token);
        logger.debug("Token JWT extraído (hash): {}", tokenHash);

        if (jwtBlacklistRepository.existsByTokenHash(tokenHash)) {
            logger.warn("Token JWT está en la lista negra: {}", token); // Añade el token (no el hash)
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT ha sido invalidado");
            return;
        }

        String email = jwtService.getEmailFromToken(token);
        if (email == null) {
            logger.warn("No se pudo extraer email del token (posiblemente expirado)");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT ha expirado");
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                Long lastLogged = authCache.get(tokenHash);
                long currentTime = System.currentTimeMillis();
                if (lastLogged == null || (currentTime - lastLogged > 60000)) {
                    logger.info("Autenticación exitosa para usuario: {}, Authorities: {}",
                            email, userDetails.getAuthorities());
                    authCache.put(tokenHash, currentTime);
                }
            } else {
                logger.warn("Token JWT inválido para usuario: {}", email);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido");
                return;
            }
        } catch (UsernameNotFoundException e) {
            logger.error("Usuario no encontrado para el token: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Usuario no encontrado");
            return;
        } catch (Exception e) {
            logger.error("Error al procesar token JWT: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Error al procesar el token JWT: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        logger.debug("Ruta original: {}, Método: {}", request.getRequestURI(), method);
        boolean shouldNotFilter = pathMatcher.match("/actuator/health", path) ||
                pathMatcher.match("/api/auth/login", path) ||
                pathMatcher.match("/api/auth/register", path) ||
                pathMatcher.match("/api/auth/google", path) ||
                pathMatcher.match("/api/calendar/callback", path) ||
                pathMatcher.match("/favicon.ico", path) ||
                (pathMatcher.match("/api/contact", path) && method.equals("POST")) ||
                (pathMatcher.match("/contact", path) && method.equals("POST")) ||
                (pathMatcher.match("/api/services/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/services/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/api/testimonials/**", path) && (method.equals("GET") || method.equals("POST"))) ||
                (pathMatcher.match("/testimonials/**", path) && (method.equals("GET") || method.equals("POST"))) ||
                (pathMatcher.match("/api/information/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/information/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/api/gallery/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/gallery/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/api/packages/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/packages/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/api/carousel/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/carousel/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/api/config/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/config/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/api/legal/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/legal/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/api/contact-info/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/contact-info/**", path) && method.equals("GET")) ||
                (pathMatcher.match("/api/cloudinary-metrics", path) && method.equals("GET"));
        logger.debug("shouldNotFilter para {} {}: {}", method, path, shouldNotFilter);
        return shouldNotFilter;
    }
}