"package a_and_s_service.compile.module.dto.role.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {
    
    private UUID id; // Cần ID để phân biệt là đang Create hay Update

    @NotBlank(message = "Mã quyền không được để trống")
    private String code;

    @NotBlank(message = "Tên quyền không được để trống")
    private String name;

    private String description;

    // Danh sách các ID của Permission được chọn từ Checkbox
    private List<UUID> permissionIds; 
}"

"package a_and_s_service.compile.module.service.role_permission.impl;

import a_and_s_service.compile.common.exception.ApiException;
import a_and_s_service.compile.common.exception.ErrorCode;
import a_and_s_service.compile.module.dto.role.request.RoleRequestDTO;
import a_and_s_service.compile.module.dto.role.response.RoleResponseDTO;
import a_and_s_service.compile.module.entity.AccountRoleEntity;
import a_and_s_service.compile.module.entity.PermissionEntity;
import a_and_s_service.compile.module.entity.RoleEntity;
import a_and_s_service.compile.module.entity.RolePermissionEntity;
import a_and_s_service.compile.module.mapper.RoleMapper;
import a_and_s_service.compile.module.repository.RoleRepository;
import a_and_s_service.compile.module.service.account_role.AccountRoleService;
import a_and_s_service.compile.module.service.role_permission.PermissionService;
import a_and_s_service.compile.module.service.role_permission.RolePermissionService;
import a_and_s_service.compile.module.service.role_permission.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final AccountRoleService accountRoleService;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;
    private final RolePermissionService rolePermissionService;

    @Override
    @Transactional
    public RoleResponseDTO insertRole(RoleRequestDTO roleRequestDTO) {
        RoleEntity role = roleMapper.toRoleEntity(roleRequestDTO);
        RoleEntity savedRole = roleRepository.save(role);

        if (roleRequestDTO.getPermissionIds() != null && !roleRequestDTO.getPermissionIds().isEmpty()) {
            List<PermissionEntity> permissionEntityList = permissionService.getAllPermissionEntityByIds(roleRequestDTO.getPermissionIds());
            Set<RolePermissionEntity> rolePermissions = permissionEntityList.stream()
                    .map(permission -> RolePermissionEntity.builder()
                            .role(savedRole)
                            .permission(permission)
                            .build()
                    ).collect(Collectors.toSet());

            rolePermissionService.saveAllRolePermission(rolePermissions);
            savedRole.setRolePermissions(rolePermissions);
        }

        return roleMapper.toRoleResponseDTO(savedRole);
    }

    @Override
    @Transactional
    public RoleResponseDTO updateRole(UUID roleId, RoleRequestDTO roleRequestDTO) {
        RoleEntity existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found"));

        existingRole.setName(roleRequestDTO.getName());
        existingRole.setDescription(roleRequestDTO.getDescription());

        // Xử lý cập nhật danh sách Permission
        if (roleRequestDTO.getPermissionIds() != null) {
            List<PermissionEntity> newPermissions = permissionService.getAllPermissionEntityByIds(roleRequestDTO.getPermissionIds());
            Set<UUID> newPermissionIds = newPermissions.stream().map(PermissionEntity::getId).collect(Collectors.toSet());

            Set<RolePermissionEntity> currentRolePermissions = existingRole.getRolePermissions();
            if (currentRolePermissions == null) {
                currentRolePermissions = new java.util.HashSet<>();
                existingRole.setRolePermissions(currentRolePermissions);
            }

            Set<UUID> currentPermIds = currentRolePermissions.stream()
                    .map(rp -> rp.getPermission().getId())
                    .collect(Collectors.toSet());

            // Xóa quyền bị bỏ check
            currentRolePermissions.removeIf(rp -> !newPermissionIds.contains(rp.getPermission().getId()));

            // Thêm quyền mới được check
            for (PermissionEntity permission : newPermissions) {
                if (!currentPermIds.contains(permission.getId())) {
                    currentRolePermissions.add(RolePermissionEntity.builder()
                            .role(existingRole)
                            .permission(permission)
                            .build());
                }
            }
        } else {
            // Nếu không gửi list lên (xóa trắng), thì clear hết
            existingRole.getRolePermissions().clear();
        }

        RoleEntity savedRole = roleRepository.save(existingRole);
        return roleMapper.toRoleResponseDTO(savedRole);
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        List<AccountRoleEntity> accountRoles = accountRoleService.findByRoleId(roleId);
        if (!accountRoles.isEmpty()) {
            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Không thể xóa Role này vì đang có User sử dụng!");
        }
        roleRepository.deleteById(roleId);
    }

    @Override
    public RoleResponseDTO getRoleById(UUID roleId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found"));
        return roleMapper.toRoleResponseDTO(role);
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponseDTO).toList();
    }
}"

"package a_and_s_service.compile.module.controller;

import a_and_s_service.compile.common.exception.ApiException;
import a_and_s_service.compile.module.dto.permission.response.PermissionResponseDTO;
import a_and_s_service.compile.module.dto.role.request.RoleRequestDTO;
import a_and_s_service.compile.module.dto.role.response.RoleResponseDTO;
import a_and_s_service.compile.module.service.role_permission.PermissionService;
import a_and_s_service.compile.module.service.role_permission.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    // --- 1. HIỂN THỊ DANH SÁCH ---
    // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.getAllRoles());
        return "role/list"; 
    }

    // --- 2. HIỂN THỊ FORM THÊM MỚI ---
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("roleDTO", new RoleRequestDTO());
        model.addAttribute("allPermissions", permissionService.getAllPermissions());
        return "role/form"; 
    }

    // --- 3. HIỂN THỊ FORM CẬP NHẬT ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RoleResponseDTO responseDTO = roleService.getRoleById(id);
            
            // Đổ data từ Response sang Request để đẩy vào Form HTML
            RoleRequestDTO requestDTO = new RoleRequestDTO();
            requestDTO.setId(responseDTO.getId());
            requestDTO.setCode(responseDTO.getCode());
            requestDTO.setName(responseDTO.getName());
            requestDTO.setDescription(responseDTO.getDescription());
            
            // Lấy ID của các permission đang có
            if (responseDTO.getPermissions() != null) {
                List<UUID> permIds = responseDTO.getPermissions().stream()
                        .map(PermissionResponseDTO::getId)
                        .collect(Collectors.toList());
                requestDTO.setPermissionIds(permIds);
            }

            model.addAttribute("roleDTO", requestDTO);
            model.addAttribute("allPermissions", permissionService.getAllPermissions());
            return "role/form";
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/roles";
        }
    }

    // --- 4. XỬ LÝ LƯU (THÊM / SỬA) ---
    @PostMapping("/save")
    public String saveRole(@Valid @ModelAttribute("roleDTO") RoleRequestDTO roleDTO,
                           BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("allPermissions", permissionService.getAllPermissions());
            return "role/form";
        }

        try {
            if (roleDTO.getId() == null) {
                roleService.insertRole(roleDTO);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm quyền thành công!");
            } else {
                roleService.updateRole(roleDTO.getId(), roleDTO);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quyền thành công!");
            }
            return "redirect:/roles"; // Redirect về danh sách (PRG Pattern)
        } catch (ApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("allPermissions", permissionService.getAllPermissions());
            return "role/form";
        }
    }

    // --- 5. XỬ LÝ XÓA (BẮT BUỘC DÙNG POST) ---
    @PostMapping("/delete/{id}")
    public String deleteRole(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa quyền thành công!");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/roles";
    }
}"

"package a_and_s_service.compile.module.mapper;

import a_and_s_service.compile.module.dto.permission.response.PermissionResponseDTO;
import a_and_s_service.compile.module.dto.role.request.RoleRequestDTO;
import a_and_s_service.compile.module.dto.role.request.RoleUpdateRequestDTO;
import a_and_s_service.compile.module.dto.role.response.RoleResponseDTO;
import a_and_s_service.compile.module.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "permissions", expression = "java(roleEntity.getRolePermissions() == null ? null : roleEntity.getRolePermissions().stream().map(rp -> new a_and_s_service.compile.module.dto.permission.response.PermissionResponseDTO(rp.getPermission().getId(), rp.getPermission().getModule(), rp.getPermission().getCode())).toList())")
    RoleResponseDTO toRoleResponseDTO(RoleEntity roleEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    RoleEntity toRoleEntity(RoleRequestDTO roleRequestDTO);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    void updateRoleFromDTO(RoleRequestDTO dto, @MappingTarget RoleEntity roleEntity);
}
"

"package a_and_s_service.compile.module.repository;

import a_and_s_service.compile.module.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    RoleEntity findByCode(String code);
}
"

"package a_and_s_service.compile.module.service.role_permission;

import a_and_s_service.compile.module.dto.role.request.RoleRequestDTO;
import a_and_s_service.compile.module.dto.role.request.RoleUpdateRequestDTO;
import a_and_s_service.compile.module.dto.role.response.RoleResponseDTO;
import a_and_s_service.compile.module.entity.RoleEntity;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleResponseDTO insertRole(RoleRequestDTO roleRequestDTO);

    RoleResponseDTO updateRole(UUID roleId, RoleUpdateRequestDTO roleRequestDTO);

    void deleteRole(UUID roleId);

    RoleResponseDTO getRoleById(UUID roleId);

    RoleEntity getRoleByIdEntity(UUID roleId);

    RoleEntity getRoleByCode(String code);

    List<RoleResponseDTO> getAllRoles();

    List<RoleEntity> getAllRoleByIds(List<UUID> roleIds);
}
"