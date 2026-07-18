package com.placardo.security;

import com.placardo.entity.User;
import com.placardo.service.AdService;
import com.placardo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Проверка владения объявлением для SpEL-выражений:
 * @PreAuthorize("@adPermissions.isOwner(#id, authentication)")
 */
@Component("adPermissions")
@RequiredArgsConstructor
public class AdPermissions {

    private final AdService adService;
    private final UserService userService;

    public boolean isOwner(Long adId, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        return user != null && adService.canManage(adService.getOrThrow(adId), user);
    }
}
