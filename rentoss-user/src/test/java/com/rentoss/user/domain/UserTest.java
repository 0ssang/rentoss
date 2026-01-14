package com.rentoss.user.domain;

import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.core.domain.model.Location;
import com.rentoss.core.common.exception.BusinessException;
import com.rentoss.user.exception.UserErrorCode;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserTest {

    @Nested
    @DisplayName("회원 생성")
    class CreateUser{

        @Test
        @DisplayName("회원 생성 시 상태는 ACTIVE이며 권한은 USER이다")
        void statusIsActive(){
            User user = createUser();

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("회원 생성 시 필수 정보가 저장된다")
        void requiredFieldsAreSaved(){
            User user = User.create(
                    "social123",
                    SocialProvider.KAKAO,
                    "test@test.com",
                    "조영상",
                    "profile.jpg"
            );

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(user.getSocialId()).isEqualTo("social123");
                softly.assertThat(user.getProvider()).isEqualTo(SocialProvider.KAKAO);
                softly.assertThat(user.getEmail()).isEqualTo("test@test.com");
                softly.assertThat(user.getNickname()).isEqualTo("조영상");
                softly.assertThat(user.getRole()).isEqualTo(UserRole.USER);
                softly.assertThat(user.getProfileImageUrl()).isEqualTo("profile.jpg");
            });
        }

        @Test
        @DisplayName("회원 생성 시 위치 정보는 null이다")
        void locationIsNull(){
            User user = createUser();

            assertThat(user.getLocation()).isNull();
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateUser{

        @Test
        @DisplayName("닉네임과 프로필 이미지를 수정한다")
        void updateNicknameAndImageUrl(){
            User user = createUser();

            user.updateProfile("새닉네임", "newimage.jpg");

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(user.getNickname()).isEqualTo("새닉네임");
                softly.assertThat(user.getProfileImageUrl()).isEqualTo("newimage.jpg");
            });
        }
    }

    @Nested
    @DisplayName("위치 정보 수정")
    class UpdateLocation{

        @Test
        @DisplayName("위치 정보를 수정한다")
        void updateLocation(){
            User user = createUser();

            Location location = Location.of(37.5665, 126.9780, "서울시 중구");
            user.updateLocation(location);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(user.getLocation()).isNotNull();
                softly.assertThat(user.getLocation().getLatitude()).isEqualTo(location.getLatitude());
                softly.assertThat(user.getLocation().getLongitude()).isEqualTo(location.getLongitude());
                softly.assertThat(user.getLocation().getAddress()).isEqualTo(location.getAddress());
            });
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class WithDraw{

        @Test
        @DisplayName("회원 탈퇴 시 상태가 WITHDRAWN으로 변경된다")
        void success(){
            User user = createUser();

            user.withdraw();

            assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("ACTIVE 상태가 아닌 회원의 탈퇴 요청은 예외가 발생한다")
        void alreadyWithdrawn() {
            User user = createUser();
            user.withdraw();

            assertThatThrownBy(user::withdraw)
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(UserErrorCode.ALREADY_WITHDRAWN);
                    });
        }

    }

    @Nested
    @DisplayName("상태 검증")
    class ValidateActive {

        @Test
        @DisplayName("탈퇴한 회원은 프로필 수정이 불가하다")
        void withdrawnUserCannotUpdateProfile() {
            User user = createUser();
            user.withdraw();

            assertThatThrownBy(() -> user.updateProfile("새닉네임", "new.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_ACTIVE);
                    });
        }

        @Test
        @DisplayName("탈퇴한 회원은 위치 수정이 불가하다")
        void withdrawnUserCannotUpdateLocation() {
            User user = createUser();
            user.withdraw();
            Location location = Location.of(37.5665, 126.9780, "서울시 중구");

            assertThatThrownBy(() -> user.updateLocation(location))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_ACTIVE);
                    });
        }
    }

    private User createUser(){
        return User.create(
                "social123",
                SocialProvider.KAKAO,
                "test@test.com",
                "조영상",
                "https://www.naver.com/images/22"
        );
    }
}
