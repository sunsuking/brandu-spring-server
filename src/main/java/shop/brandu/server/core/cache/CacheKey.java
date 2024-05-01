package shop.brandu.server.core.cache;

public class CacheKey {
    public static String authenticationKey(String username) {
        return "authentication#" + username;
    }

    public static String emailCodeKey(String email) {
        return "emailConfirmCode#" + email;
    }
}
