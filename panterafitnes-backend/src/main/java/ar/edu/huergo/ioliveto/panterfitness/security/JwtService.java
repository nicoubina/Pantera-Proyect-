package ar.edu.huergo.ioliveto.panterfitness.security;

import ar.edu.huergo.ioliveto.panterfitness.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	@Value("${security.jwt.secret}")
	private String secret;

	@Value("${security.jwt.expiration-ms}")
	private long expirationMs;

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		if (userDetails instanceof Usuario usuario) {
			claims.put("id", usuario.getId());
			claims.put("rol", usuario.getRol().name());
		}
		return buildToken(claims, userDetails);
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private String buildToken(Map<String, Object> claims, UserDetails userDetails) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(userDetails.getUsername())
				.setIssuedAt(now)
				.setExpiration(expiration)
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	private boolean isTokenExpired(String token) {
		return extractClaim(token, Claims::getExpiration).before(new Date());
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claimsResolver.apply(claims);
	}

	private Key getSigningKey() {
		try {
			byte[] keyBytes = Decoders.BASE64.decode(secret);
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (IllegalArgumentException exception) {
			return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}
	}
}
