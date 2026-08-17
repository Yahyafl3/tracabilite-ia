CREATE TABLE IF NOT EXISTS backup_job (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id UUID,
    created_by_email VARCHAR(255),
    filename VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    pack_sha256 VARCHAR(64) NOT NULL,
    decision_count INTEGER NOT NULL,
    user_count INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    last_verified_at TIMESTAMP,
    last_restored_at TIMESTAMP,
    restore_users_created INTEGER,
    restore_users_skipped INTEGER,
    restore_decisions_created INTEGER,
    restore_decisions_skipped INTEGER
);
