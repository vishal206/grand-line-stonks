package com.grandlinestonks.auth;

import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.auth.dto.AuthResponse;
import com.grandlinestonks.auth.dto.MeResponse;
import com.grandlinestonks.common.Berries;
import com.grandlinestonks.error.InvalidCredentialsException;
import com.grandlinestonks.error.UsernameTakenException;
import com.grandlinestonks.user.User;
import com.grandlinestonks.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameTakenException(username);
        }
        User user = userRepository.save(new User(username, passwordEncoder.encode(password)));
        accountRepository.save(new Account(user.getId(), Berries.STARTING_BERRIES));
        return new AuthResponse(jwtService.issueToken(user.getId(), user.getUsername()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(jwtService.issueToken(user.getId(), user.getUsername()));
    }

    @Transactional(readOnly = true)
    public MeResponse me(AuthenticatedUser authenticatedUser) {
        Account account = accountRepository.findByUserId(authenticatedUser.userId())
                .orElseThrow(InvalidCredentialsException::new);
        return new MeResponse(
                authenticatedUser.userId(), authenticatedUser.username(), account.getBalance());
    }
}
