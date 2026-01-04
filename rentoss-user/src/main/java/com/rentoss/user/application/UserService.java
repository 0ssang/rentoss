package com.rentoss.user.application;

import com.rentoss.core.domain.model.Location;
import com.rentoss.core.common.exception.BusinessException;
import com.rentoss.user.domain.SocialProvider;
import com.rentoss.user.domain.User;
import com.rentoss.user.domain.UserRepository;
import com.rentoss.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public User findOrCreateUser(String socialId, SocialProvider provider, String email,
                                 String nickname, String profileImageUrl) {
        return userRepository.findBySocialIdAndProvider(socialId, provider)
                .orElseGet(() -> {
                    User user = User.create(socialId, provider, email, nickname, profileImageUrl);
                    return userRepository.save(user);
                });
    }

    @Transactional
    public void updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = getUser(userId);

        if(!user.getNickname().equals(nickname) && userRepository.existsByNickname(nickname)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateProfile(nickname, profileImageUrl);
    }

    @Transactional
    public void updateLocation(Long userId, Double latitude, Double longitude, String address) {
        User user = getUser(userId);
        user.updateLocation(Location.of(latitude, longitude, address));
    }

    @Transactional
    public void withdraw(Long userId){
        User user = getUser(userId);
        user.withdraw();
    }
}
