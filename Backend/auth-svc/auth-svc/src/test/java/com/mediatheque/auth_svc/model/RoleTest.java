package com.mediatheque.auth_svc.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleTest {

    @Test
    void role_shouldHaveTwoValues() {
        assertThat(Role.values()).hasSize(2);
    }

    @Test
    void role_shouldContainUser() {
        assertThat(Role.valueOf("USER")).isEqualTo(Role.USER);
    }

    @Test
    void role_shouldContainAdmin() {
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
    }

    @Test
    void role_ordinal_userShouldBeFirst() {
        assertThat(Role.USER.ordinal()).isEqualTo(0);
    }

    @Test
    void role_ordinal_adminShouldBeSecond() {
        assertThat(Role.ADMIN.ordinal()).isEqualTo(1);
    }

    @Test
    void role_name_shouldMatchLiteral() {
        assertThat(Role.USER.name()).isEqualTo("USER");
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
    }

    @Test
    void role_valueOf_shouldThrow_whenInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("SUPERADMIN"));
    }

    @Test
    void role_shouldBeEqualToItself() {
        assertThat(Role.USER).isEqualTo(Role.USER);
        assertThat(Role.ADMIN).isEqualTo(Role.ADMIN);
    }

    @Test
    void role_userAndAdmin_shouldNotBeEqual() {
        assertThat(Role.USER).isNotEqualTo(Role.ADMIN);
    }
}