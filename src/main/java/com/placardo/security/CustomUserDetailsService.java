package com.placardo.security;

import com.placardo.entity.AuthProvider;
import com.placardo.entity.User;
import com.placardo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
        if (user.getPasswordHash() == null && user.getProvider() == AuthProvider.GOOGLE) {
            throw new UsernameNotFoundException("Этот аккаунт входит через Google");
        }
        return new AppUserDetails(user);
    }
}
