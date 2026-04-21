package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.auth.request.RegisterRequestDTO;

public interface AuthService {
    void register(RegisterRequestDTO request);
}
