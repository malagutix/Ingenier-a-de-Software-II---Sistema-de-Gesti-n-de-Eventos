package modelo.usuarios.repositorio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import modelo.usuarios.entidades.TokenRecuperacion;

public class TokenRepositoryEnMemoria implements TokenRepository {

    private final Map<String, TokenRecuperacion> tokens = new HashMap<>();

    @Override
    public synchronized void guardar(TokenRecuperacion token) {
        tokens.put(token.getCodigo(), token);
    }

    @Override
    public synchronized Optional<TokenRecuperacion> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(tokens.get(codigo));
    }

    @Override
    public synchronized void actualizar(TokenRecuperacion token) {
        tokens.put(token.getCodigo(), token);
    }
}
