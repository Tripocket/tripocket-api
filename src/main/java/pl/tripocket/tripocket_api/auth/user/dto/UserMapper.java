package pl.tripocket.tripocket_api.auth.user.dto;

import org.mapstruct.Mapper;
import pl.tripocket.tripocket_api.auth.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserSearchResponse toUserSearchResponse(User user);
}
