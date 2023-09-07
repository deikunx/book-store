package com.bookstore.repository.role;

import com.bookstore.model.Role;
import com.bookstore.model.RoleName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Find role by name")
    void findRoleByName_ShouldReturnRoleByName() {
        Role role = new Role();
        role.setName(RoleName.ROLE_USER);
        Role expected = roleRepository.save(role);

        Role actual = roleRepository.findRoleByName(RoleName.ROLE_USER);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.getName(), actual.getName());
    }
}
