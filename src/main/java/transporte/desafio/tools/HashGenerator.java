package transporte.desafio.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Ferramenta utilitaria para gerar hashes BCrypt (ex: senha admin).
 * Executar: mvn compile exec:java -Dexec.mainClass=transporte.desafio.tools.HashGenerator
 */
public final class HashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senha = args.length > 0 ? args[0] : "admin123";
        String hash = encoder.encode(senha);
        System.out.println("============================================");
        System.out.println("Senha: " + senha);
        System.out.println("BCrypt: " + hash);
        System.out.println("Matches: " + encoder.matches(senha, hash));
        System.out.println("============================================");
    }
}
