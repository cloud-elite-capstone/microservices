CREATE TABLE IF NOT EXISTS conversations (
    id                 uuid PRIMARY KEY,
    user_id            uuid,
    history            text,
    system_instruction text,
    created_at         timestamptz,
    updated_at         timestamptz
);
