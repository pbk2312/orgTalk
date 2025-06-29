-- resources/schema.sql

CREATE TABLE IF NOT EXISTS chat_room (
                                         id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         organization_id BIGINT       NOT NULL,
                                         name            VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(50)  NOT NULL,
    owner_id        BIGINT       NOT NULL,
    password        VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX uq_chatroom_name (name),

    INDEX idx_chatroom_orgid_type_created_updated (
                                                      organization_id,
                                                      type,
                                                      created_at,
                                                      updated_at
                                                  ),

    INDEX idx_chatroom_orgid_created_updated (
                                                 organization_id,
                                                 created_at DESC,
                                                 updated_at DESC
                                             ),

    INDEX idx_chatroom_orgid_name_pref (
                                           organization_id,
                                           name(10)
    ),

    FULLTEXT INDEX idx_chatroom_search (
                                           name,
                                           description
                                       )
    )
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci;









CREATE TABLE IF NOT EXISTS chat_room_member (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                chat_room_id BIGINT NOT NULL,
                                                member_id BIGINT NOT NULL,
                                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                CONSTRAINT fk_chat_room_member_chat_room FOREIGN KEY (chat_room_id)
    REFERENCES chat_room(id) ON DELETE CASCADE,
    INDEX idx_crm_chat_room_id  (chat_room_id),
    INDEX idx_crm_member_id     (member_id),
    INDEX idx_crm_member_room   (member_id, chat_room_id)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci;
