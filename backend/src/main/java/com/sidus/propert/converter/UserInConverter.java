package com.sidus.propert.converter;

import com.sidus.propert.dto.UserInDTO;
import com.sidus.propert.model.entity.Project;
import com.sidus.propert.model.entity.User;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;

public class UserInConverter implements Converter<UserInDTO, User> {
    @Override
    public User convert(UserInDTO source) {
        User user = new User();
        user.setId(source.id());
        user.setUsername(source.username());
        user.setPassword(source.password());
        return user;
    }
}
