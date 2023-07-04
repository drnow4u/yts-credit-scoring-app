package com.yolt.creditscoring.controller.customer;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.customer.ClientTokenPrincipal;
import com.yolt.creditscoring.controller.admin.users.InviteUserDTO;
import com.yolt.creditscoring.controller.exception.ErrorResponseDTO;
import com.yolt.creditscoring.controller.exception.FormValidationErrorResponse;
import com.yolt.creditscoring.controller.exception.Violation;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.usecase.UserManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.springdoc.annotations.ExternalApi;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_TOKEN)
public class CustomerAPIController {

    public static final String INVITE_USER_CLIENT_TOKEN_ENDPOINT = "/api/customer/users/invite";
    public static final String DELETE_USER_ENDPOINT = "/api/customer/v1/users/{creditScoreUserId}";

    private final UserManagementUseCase userManagementUseCase;

    @Operation(
            summary = "Invite user",
            description = "Invite a user. An email will be sent to the provided email. The API returns a user-id that can be used to " +
                    "retrieve the credit score report once it is available.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful",
                            content = {@Content(schema = @Schema(implementation = CustomerAPIUserInvitationDTO.class))}
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation errors on request body",
                            content = {@Content(schema = @Schema(implementation = FormValidationErrorResponse.class))}
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "wrong clientEmailId",
                            content = {@Content(schema = @Schema(implementation = ErrorResponseDTO.class))}
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized.",
                            content = {@Content(schema = @Schema(implementation = ErrorResponseDTO.class))}
                    )
            })
    @PreAuthorize("hasAuthority('" + ClientTokenPermission.Permissions.INVITE_USER + "')")
    @ExternalApi
    @PostMapping(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
    public CustomerAPIUserInvitationDTO inviteUserByClientToken(@Valid @RequestBody InviteUserDTO inviteUserDTO,
                                                                @Parameter(hidden = true) @AuthenticationPrincipal ClientTokenPrincipal principal) {
        return new CustomerAPIUserInvitationDTO(userManagementUseCase.inviteNewUser(
                inviteUserDTO,
                principal.getClientId(),
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(),
                principal.getTokenId(),
                principal.getEmail(),
                principal.getClientAccessType()));
    }

    @Operation(
            summary = "Delete user",
            description = "Delete a user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful",
                            content = {@Content(schema = @Schema(implementation = CustomerAPIUserInvitationDTO.class))}
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User (creditScoreUserId) not found",
                            content = {@Content(schema = @Schema(implementation = ErrorResponseDTO.class))}
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized.",
                            content = {@Content(schema = @Schema(implementation = ErrorResponseDTO.class))}
                    ),

            })
    @PreAuthorize("hasAuthority('" + ClientTokenPermission.Permissions.DELETE_USER + "')")
    @ExternalApi
    @DeleteMapping(DELETE_USER_ENDPOINT)
    public void deleteUser(@PathVariable UUID creditScoreUserId,
                           @Parameter(hidden = true) @AuthenticationPrincipal ClientTokenPrincipal principal) {
        userManagementUseCase.deleteUserByUserID(
                creditScoreUserId,
                principal.getClientId(),
                principal.getTokenId(),
                principal.getEmail(),
                principal.getClientAccessType());
    }

    /**
     * An exception handler for this controller. We return validation errors to the frontend that relies on the validation messages and this structure.
     * We don't want to globally override the lovebird commons {@link nl.ing.lovebird.errorhandling.config.BaseExceptionHandlers#handleMethodArgumentNotValidException(MethodArgumentNotValidException)}
     * more safe version that does not expose messages, but since we need them in the frontend, we make an exception for this controller.
     *
     * @param ex The exception
     * @return The response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public FormValidationErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return FormValidationErrorResponse.builder()
                .violations(ex.getBindingResult().getFieldErrors()
                        .stream()
                        .map(field -> new Violation(field.getField(), field.getDefaultMessage()))
                        .toList())
                .build();
    }
}
