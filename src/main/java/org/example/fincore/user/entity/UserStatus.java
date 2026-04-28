package org.example.fincore.user.entity;

public enum UserStatus {
    ACTIVE,
    INACTIVE;

    public boolean canChangeTo(UserStatus newStatus) {
        return switch (this) {
            case ACTIVE -> newStatus == UserStatus.INACTIVE;
            default -> false;
        };
    }
}
