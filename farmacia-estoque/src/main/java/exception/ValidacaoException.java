package exception;

/** Lançada quando algum dado de entrada não respeita as regras de negócio. */
public class ValidacaoException extends Exception {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
