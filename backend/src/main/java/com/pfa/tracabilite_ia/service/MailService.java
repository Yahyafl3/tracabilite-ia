package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.SupportMessage;

public interface MailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);

    void sendSupportNotification(SupportMessage supportMessage);
}
