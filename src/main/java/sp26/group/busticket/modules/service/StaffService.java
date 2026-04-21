package sp26.group.busticket.modules.service;

import org.springframework.data.domain.Page;
import sp26.group.busticket.modules.dto.account.request.StaffCreateRequestDTO;
import sp26.group.busticket.modules.dto.account.request.StaffUpdateRequestDTO;
import sp26.group.busticket.modules.dto.account.response.StaffResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffService {
    StaffResponseDTO createStaff(StaffCreateRequestDTO request);

    Page<StaffResponseDTO> getStaffPage(String keyword, String role, int page, int size);

    List<StaffResponseDTO> getAllStaff();

    Optional<StaffResponseDTO> getStaffById(UUID staffId);

    Optional<StaffResponseDTO> updateStaff(UUID staffId, StaffUpdateRequestDTO request);

    boolean deleteStaff(UUID staffId);
}


