package com.ncc.kairos.moirai.zeus.api;

import com.ncc.kairos.moirai.zeus.dao.JwtPermissionRepository;
import com.ncc.kairos.moirai.zeus.dao.JwtRoleRepository;
import com.ncc.kairos.moirai.zeus.model.*;
import com.ncc.kairos.moirai.zeus.security.payloads.JwtUserDetails;
import com.ncc.kairos.moirai.zeus.security.utils.JwtUtils;
import com.ncc.kairos.moirai.zeus.services.AdminServices;
import com.ncc.kairos.moirai.zeus.services.KairosUserService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
/**
 * Controller Implementation of the Admin Services that is autogenerated by
 * swagger-codegen.
 * @author Lion Tamer
 */
@Controller
@RequestMapping("${openapi.moiraiZeus.base-path:}")
public class AdminApiController implements AdminApi {

    @Autowired
    private AdminServices adminServices;

    @Autowired
    private KairosUserService kairosUserService;

    @Autowired
    private JwtPermissionRepository jwtPermissionService;

    @Autowired
    private JwtRoleRepository jwtRoleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private final NativeWebRequest request;

    @Autowired
    public AdminApiController(NativeWebRequest request) {
        this.request = request;
    }

    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(this.request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASSUME-USER')")
    public ResponseEntity<StringResponse> assumeUser(@ApiParam(value = "Desired Username"  )  @Valid @RequestBody StringRequest stringRequest) {

        UserDetails userDetails;
        try {
            userDetails = this.kairosUserService.loadUserByUsername(stringRequest.getValue());
        } catch(Exception e) {
            userDetails = null;
        }

        if (userDetails != null) {
            JwtUserDetails jwtUserDetails = new JwtUserDetails();
            jwtUserDetails.setUsername(userDetails.getUsername());
            Collection<? extends GrantedAuthority> grantAuthorities = userDetails.getAuthorities();
            String[] authArray = new String[grantAuthorities.size() + 1];
            int i = 0;
            for (GrantedAuthority g : grantAuthorities) {
                authArray[i++] = g.toString();
            }
            authArray[i++] = "ASSUME-USER";
            jwtUserDetails.setAuthorities(authArray);
            String jwt = jwtUtils.generateJwtToken(jwtUserDetails);
            return new ResponseEntity<>(new StringResponse().value(jwt), HttpStatus.OK);
        }
        return new ResponseEntity<>(new StringResponse().value("No matching user found."), HttpStatus.NOT_FOUND);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> provisionGitlab(@ApiParam(value = "Launches Terraform with the selected parameters")
                                                          @Valid @RequestBody GitLabRequest gitLabRequest) {
        try {
            this.adminServices.provisionGitlab(gitLabRequest);
            return new ResponseEntity<>(new StringResponse().value("Gitlab has been provisioned successfully."), HttpStatus.OK);
        } catch(Exception e) {
            return new ResponseEntity<>(new StringResponse().value("Error: Unable to Provision Gitlab"), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> destroyGitlab() {
       
        try {
            this.adminServices.destroyGitlab();
            return new ResponseEntity<>(new StringResponse().value("Gitlab has been destroyed successfully."), HttpStatus.OK);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch(Exception e) {
            return new ResponseEntity<>(new StringResponse().value("Unable to Destroy GitLab"), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> provisionEnclaveInfrastructure(
        @ApiParam(value = "Launches Terraform with the selected parameters")  
        @Valid @RequestBody StringRequest stringRequest) {

        try {
            this.adminServices.provisionEnclaveInfrastructure();
            return new ResponseEntity<>(new StringResponse().value("Enclave Infrastructure has been provisioned successfully."), HttpStatus.OK);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch(Exception e) {
            return new ResponseEntity<>(new StringResponse().value("Unable to create Enclave Infrastructure"), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> destroyEnclaveInfrastructure() {
        try {
            this.adminServices.destroyEnclaveInfrastructure();
            return new ResponseEntity<>(new StringResponse().value("Enclave Infrastructure has been destroyed successfully."), HttpStatus.OK);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch(Exception e) {
            return new ResponseEntity<>(new StringResponse().value("Unable to destroy Enclave Infrastructure"), HttpStatus.BAD_REQUEST);
        }
    }  

    /**
     *  Will return the full list of users and their permissions. Only Available for users with admin permissions.
     * @return Success or Unauthorized
     */
    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<JwtUser>> listUsers() {
        List<JwtUser> userList = this.kairosUserService.findAllUsers();
        return new ResponseEntity<>(userList, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<JwtPermission>> listPermissions() {
        List<JwtPermission> permissionList = this.jwtPermissionService.findAll();
        return new ResponseEntity<>(permissionList, HttpStatus.OK);
    }
    
    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<JwtRole>> listRoles() {
        List<JwtRole> roleList = this.jwtRoleRepository.findAll();
        return new ResponseEntity<>(roleList, HttpStatus.OK);
    }

    /**
     * Handles updating user permissions for any user given this is only being used by authorized admins.
     *
     * @param username The name of the user whos permissions are being updated.
     * @param jwtRole The Roles for a given user.
     * @return Success or Unauthorized.
     */
    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> updateUserAccount(
            @ApiParam(value = "FAQ category to update to"  )  @Valid @RequestBody UserDataDto userData) {
        this.adminServices.updateUserAccount(userData);
        return new ResponseEntity<>(new StringResponse().value("Assigned new Roles(s) successfully."), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> createOrUpdateRole(@RequestBody List<JwtRole> jwtRole) {
        this.adminServices.createOrUpdateRoles(jwtRole);
        return new ResponseEntity<>(new StringResponse().value("Saved new Roles(s) successfully"), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> deleteRoles(@ApiParam(value = "roles to delete"  )  @Valid @RequestBody JwtRole jwtRole) {
        this.adminServices.deleteRoles(jwtRole);
        return new ResponseEntity<>(new StringResponse().value("Saved new Roles(s) successfully."), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> mergeTeam(
            @ApiParam(value = "teamName to assign to user") @Valid @RequestParam(value = "teamName", required = false) String teamName, 
            @ApiParam(value = "user to save"  )  @Valid @RequestBody JwtUser jwtUser) {
        this.adminServices.migrateTeamName(jwtUser, teamName);
        return new ResponseEntity<>(new StringResponse().value("Saved new teamname successfully."), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> updateTeamName(
            @ApiParam(value = "teamName to assign to user") @Valid @RequestParam(value = "teamName", required = false) String teamName, 
            @ApiParam(value = "user to save"  )  @Valid @RequestBody JwtUser jwtUser) {
        this.adminServices.updateTeamName(jwtUser, teamName);
        return new ResponseEntity<>(new StringResponse().value("Saved new teamname successfully."), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> deleteTeamRegistry(@ApiParam(value = "teamName to assign to user") @Valid @RequestParam(value = "teamName", required = false) String teamName) {
        this.adminServices.deleteDockerRegistry(teamName);
        return new ResponseEntity<>(new StringResponse().value("Deleted docker registry successfully."), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StringResponse> toggleAccountActivation(@ApiParam(value = "User to flip Account Active flag") @Valid @RequestParam(value = "userName", required = false) String userName) {
        this.adminServices.toggleAccountActivation(userName);
        return new ResponseEntity<>(new StringResponse().value("Flipped Account Activation Flag"), HttpStatus.OK);
    }

}
