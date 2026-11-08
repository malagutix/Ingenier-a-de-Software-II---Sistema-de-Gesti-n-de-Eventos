package modelo.usuarios.repositorio;

import java.util.Optional;

import modelo.usuarios.entidades.TokenRecuperacion;

/** Patrón Repository para los códigos de recuperación de contraseña. */
public interface TokenRepository {
    void guardar(TokenRecuperacion token);
    Optional<TokenRecuperacion> buscarPorCodigo(String codigo);
    void actualizar(TokenRecuperacion token);
}
