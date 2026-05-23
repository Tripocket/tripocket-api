package pl.tripocket.tripocket_api.auth.user.service;

import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tripocket.tripocket_api.auth.user.dto.ProviderUserInfo;
import pl.tripocket.tripocket_api.auth.user.dto.UserMapper;
import pl.tripocket.tripocket_api.auth.user.dto.UserSearchResponse;
import pl.tripocket.tripocket_api.auth.user.model.User;
import pl.tripocket.tripocket_api.auth.user.model.UserIdentityProvider;
import pl.tripocket.tripocket_api.auth.user.repository.UserIdentityProviderRepository;
import pl.tripocket.tripocket_api.auth.user.repository.UserRepository;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserIdentityProviderRepository identityProviderRepository;
  private final UserMapper userMapper;

  public UserService(
      UserRepository userRepository,
      UserIdentityProviderRepository identityProviderRepository,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.identityProviderRepository = identityProviderRepository;
    this.userMapper = userMapper;
  }

  @Cacheable(value = "knownUsers", key = "#sub.toString()")
  @Transactional
  public boolean ensureExists(UUID sub, ProviderUserInfo info) {
    if (!userRepository.existsById(sub)) {
      User user = new User();
      user.setId(sub);
      user.setEmail(info.email());
      user.setUsername(info.username());
      user.setDisplayName(info.displayName());
      user.setAvatarUrl(info.avatarUrl());
      userRepository.save(user);

      UserIdentityProvider idp = new UserIdentityProvider();
      idp.setId(UUID.randomUUID());
      idp.setUser(user);
      idp.setEmail(info.email());
      idp.setProvider(info.provider());
      idp.setProviderId(info.providerId());
      identityProviderRepository.save(idp);
    }
    return true;
  }

  public UserSearchResponse searchByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .map(userMapper::toUserSearchResponse)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}
