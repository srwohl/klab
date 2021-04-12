package org.integratedmodelling.klab.hub.payload;

import javax.validation.constraints.NotBlank;

public class InvitedUserSignup {

    public InvitedUserSignup() {

    }

    public InvitedUserSignup( @NotBlank String username, @NotBlank String email, @NotBlank String password,
            @NotBlank String confirm ) {
        super();
        this.username = username;
        this.email = email;
        this.newPassword = password;
        this.confirm = confirm;
    }

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirm;

    @NotBlank
    private String username;

    @NotBlank
    private String email;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirm() {
        return confirm;
    }

}
