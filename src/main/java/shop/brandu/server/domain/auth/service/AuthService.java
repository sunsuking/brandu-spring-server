package shop.brandu.server.domain.auth.service;

import shop.brandu.server.domain.auth.dto.AuthData.JwtToken;
import shop.brandu.server.domain.auth.dto.AuthData.SignIn;
import shop.brandu.server.domain.auth.dto.AuthData.SignUp;
import shop.brandu.server.domain.user.entity.User;

public interface AuthService {
    JwtToken signIn(SignIn signIn);

    void signUp(SignUp signUp);

    void signOut(User user, String refreshToken);

    boolean confirm(String type, String email, String code);

    void findPassword(String email);

    void resendEmail(String email, String type);

    JwtToken refresh(String refreshToken);
}
