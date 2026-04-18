package pl.tripocket.tripocket_api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
  @GetMapping("/test")
  public String getMethodName(Authentication authentication) {
    return new String(authentication.getName());
  }
}
