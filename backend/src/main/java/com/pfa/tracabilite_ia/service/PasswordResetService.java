package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.ForgotPasswordRequest;
import com.pfa.tracabilite_ia.dto.request.ResetPasswordRequest;
import com.pfa.tracabilite_ia.dto.response.MessageResponse;

public interface PasswordResetService {
    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);
}
