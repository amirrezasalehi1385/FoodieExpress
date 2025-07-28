
package org.FoodOrder.server.service;

import org.FoodOrder.server.DTO.UserDto;
import org.FoodOrder.server.models.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    public static UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getAddress(),
                user.getPhone(),
                user.getEmail(),
                user.getProfileImageBase64(),
                user.getRole(),
                user.getApprovalStatus().toString()
        );
    }

    public static List<UserDto> convertToDtoList(List<User> users) {
        return users.stream()
                .map(UserService::convertToDto)
                .collect(Collectors.toList());
    }
}