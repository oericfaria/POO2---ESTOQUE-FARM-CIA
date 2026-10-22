package exception;

/** Lançada quando ocorre erro de leitura/escrita no arquivo CSV. */
public class PersistenciaException extends Exception {
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
