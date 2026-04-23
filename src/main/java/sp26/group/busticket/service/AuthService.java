package sp26.group.busticket.service;

import sp26.group.busticket.dto.auth.request.RegisterRequestDTO;

public interface AuthService {
    void register(RegisterRequestDTO request);
}
