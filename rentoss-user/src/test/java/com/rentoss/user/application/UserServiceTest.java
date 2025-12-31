package com.rentoss.user.application;

import com.rentoss.core.exception.BusinessException;
import com.rentoss.user.domain.SocialProvider;
import com.rentoss.user.domain.User;
import com.rentoss.user.domain.UserRepository;
import com.rentoss.user.domain.UserStatus;
import com.rentoss.user.exception.UserErrorCode;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("회원 조회")
    class GetUser{

        @Test
        @DisplayName("존재하는 회원을 조회한다")
        void success() {
            User user = createUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            User result = userService.getUser(1L);

            assertThat(result.getNickname()).isEqualTo(user.getNickname());
        }

        @Test
        @DisplayName("존재하지 않는 회원 조회 시 예외가 발생한다")
        void userNotFound() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("회원 생성 또는 조회")
    class FindOrCreateUser{

        @Test
        @DisplayName("기존 회원이 존재하면 조회한다")
        void findExistingUser() {
            User user = createUser();
            given(userRepository.findBySocialIdAndProvider("social123", SocialProvider.KAKAO)).willReturn(Optional.of(user));

            User result = userService.findOrCreateUser(
                    user.getSocialId(), user.getProvider(), user.getEmail(), user.getNickname(), user.getProfileImageUrl()
            );

            assertThat(result.getNickname()).isEqualTo(user.getNickname());
        }

        @Test
        @DisplayName("기존 회원이 없으면 생성한다.")
        void createNewUser() {
            given(userRepository.findBySocialIdAndProvider("social123", SocialProvider.KAKAO))
                    .willReturn(Optional.empty());
            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            User result = userService.findOrCreateUser(
                    "social123", SocialProvider.KAKAO, "test@test.com", "조영상", "profile.jpg"
            );

            assertThat(result.getNickname()).isEqualTo("조영상");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("프로필 수정")
    class UpdateProfile {

        @Test
        @DisplayName("프로필을 수정한다")
        void success() {
            User user = createUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByNickname("새닉네임")).willReturn(false);

            userService.updateProfile(1L, "새닉네임", "new-profile.jpg");

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(user.getNickname()).isEqualTo("새닉네임");
                softly.assertThat(user.getProfileImageUrl()).isEqualTo("new-profile.jpg");
            });
        }

        @Test
        @DisplayName("중복되는 닉네임으로 수정 시 예외가 발생한다")
        void duplicateNickname() {
            User user = createUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

            assertThatThrownBy(() -> userService.updateProfile(1L, "중복닉네임", "profile.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(UserErrorCode.DUPLICATE_NICKNAME);
                    });
        }

        @Test
        @DisplayName("닉네임이 동일하면 중복 검사를 수행하지 않는다")
        void sameNicknameSkipsDuplicateCheck() {
            User user = createUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            userService.updateProfile(1L, "조영상", "new-profile.jpg");

            assertThat(user.getProfileImageUrl()).isEqualTo("new-profile.jpg");
        }
    }

    @Nested
    @DisplayName("위치 정보 수정")
    class UpdateLocation {

        @Test
        @DisplayName("위치 정보를 수정한다")
        void success() {
            User user = createUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            userService.updateLocation(1L, 37.5665, 126.9780, "서울시 중구");

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(user.getLocation()).isNotNull();
                softly.assertThat(user.getLocation().getLatitude()).isEqualTo(37.5665);
                softly.assertThat(user.getLocation().getLongitude()).isEqualTo(126.9780);
                softly.assertThat(user.getLocation().getAddress()).isEqualTo("서울시 중구");
            });
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {

        @Test
        @DisplayName("회원을 탈퇴 처리한다")
        void success() {
            User user = createUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            userService.withdraw(1L);
            assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        }
    }

    private User createUser() {
        return User.create(
                "social123",
                SocialProvider.KAKAO,
                "test@test.com",
                "조영상",
                "profile.jpg"
        );
    }
}
