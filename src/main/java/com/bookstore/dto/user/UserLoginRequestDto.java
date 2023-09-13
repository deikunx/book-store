package com.bookstore.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserLoginRequestDto {
    @NotEmpty
    @Email
    @Size(min = 4, max = 50)
    private String email;

    @NotEmpty
    @Size(min = 6, max = 100)
    private String password;
}
