package util;

/** Utilitários para CNPJ: normalização, validação dos dígitos verificadores e máscara. */
public final class CnpjUtil {

    private static final int[] PESO_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESO_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private CnpjUtil() { }

    /** Remove tudo que não for dígito. */
    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    public static boolean isValido(String cnpj) {
        String d = somenteDigitos(cnpj);
        if (d.length() != 14) return false;
        if (d.chars().distinct().count() == 1) return false; // 00000000000000, 11111111111111...

        int dv1 = calcularDigito(d, PESO_1);
        int dv2 = calcularDigito(d, PESO_2);
        return dv1 == Character.getNumericValue(d.charAt(12))
            && dv2 == Character.getNumericValue(d.charAt(13));
    }

    private static int calcularDigito(String digitos, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /** Formata 14 dígitos como 00.000.000/0000-00. Se não tiver 14 dígitos, devolve o texto original. */
    public static String formatar(String cnpj) {
        String d = somenteDigitos(cnpj);
        if (d.length() != 14) return cnpj;
        return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8)
             + "/" + d.substring(8, 12) + "-" + d.substring(12);
    }
}
