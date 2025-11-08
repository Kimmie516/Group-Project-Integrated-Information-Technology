package sit.int221.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String errorType = (String) request.getAttribute("auth_error");
        int status = HttpServletResponse.SC_UNAUTHORIZED;
        String message = "Unauthorized";

        if ("MISSING".equals(errorType)) {
            message = "Authorization token is required";
        } else if ("INVALID".equals(errorType)) {
            message = "Invalid authorization token";
        } else if ("EXPIRED".equals(errorType)) {
            message = "Token has expired";
        }

        response.setStatus(status);
        response.setContentType("application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("error", "Unauthorized");
        body.put("message", message);
        body.put("path", request.getRequestURI());

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
