package com.sidus.propert.converter;

import com.sidus.propert.dto.UserDTO;
import com.sidus.propert.dto.UserInDTO;
import com.sidus.propert.model.entity.Task;
import com.sidus.propert.model.entity.User;
import org.springframework.core.convert.converter.Converter;

public class UserConverter implements Converter<User, UserDTO> {
    @Override
    public UserDTO convert(User source) {
        return new UserDTO(
                source.getId(),
                source.getUsername(),
                source.getRole());
    }
}
