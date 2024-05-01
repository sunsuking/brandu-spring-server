package shop.brandu.server.domain.auth.service;

import shop.brandu.server.domain.auth.dto.AuthData.JwtToken;
import shop.brandu.server.domain.auth.dto.AuthData.SignIn;
import shop.brandu.server.domain.auth.dto.AuthData.SignUp;
import shop.brandu.server.domain.user.entity.User;

public interface AuthService {
    JwtToken signIn(SignIn signIn);

    void signUp(SignUp signUp);

    void signOut(User user, JwtToken token);

    boolean confirm(String email, String code);
}
