CREATE TABLE public.fulfillment_form
(
  fulfillment_form_id                         UUID           NOT NULL PRIMARY KEY,
  client_member_id                            UUID           NOT NULL REFERENCES member,
  agent_member_id                             UUID           NOT NULL REFERENCES member,
  concert_concert_id                          UUID           NOT NULL REFERENCES concert,
  application_form_application_form_id        UUID           NOT NULL REFERENCES application_form,
  chat_room_id                                VARCHAR(255)   NOT NULL,
  agent_bank_account_agent_bank_account_id    UUID           NOT NULL REFERENCES agent_bank_account,
  particular_memo                             VARCHAR(100)   NOT NULL,
  fulfillment_form_status                     VARCHAR(255)   NOT NULL
    CONSTRAINT fulfillment_form_status_check
      CHECK ( fulfillment_form_status IN (
                                          'PENDING_FULFILLMENT_FORM',
                                          'ACCEPTED_FULFILLMENT_FORM',
                                          'UPDATE_FULFILLMENT_FORM',
                                          'REJECTED_FULFILLMENT_FORM'
        )),
  created_date                                TIMESTAMPTZ(0) NOT NULL,
  updated_date                                TIMESTAMPTZ(0) NOT NULL,
  deleted                                     BOOLEAN        NOT NULL DEFAULT FALSE,
  deleted_date                                TIMESTAMPTZ(0)
);

CREATE TABLE public.fulfillment_form_img
(
  fulfillment_form_img_id                     UUID           NOT NULL PRIMARY KEY,
  fulfillment_form_fulfillment_form_id        UUID           NOT NULL REFERENCES fulfillment_form,
  original_filename                           VARCHAR(255)   NOT NULL,
  stored_path                                 VARCHAR(512)   NOT NULL UNIQUE,
  file_extension                              VARCHAR(255)   NOT NULL
    CONSTRAINT fulfillment_form_img_file_extension_check
      CHECK ( (file_extension)::TEXT = ANY ((
        ARRAY [
        'JPG'::VARCHAR,
        'JPEG'::VARCHAR,
        'PNG'::VARCHAR,
        'GIF'::VARCHAR,
        'BMP'::VARCHAR,
        'WEBP'::VARCHAR
        ]
        )::TEXT[] )),
  size_bytes                                  BIGINT         NOT NULL,
  created_date                                TIMESTAMPTZ(0) NOT NULL,
  updated_date                                TIMESTAMPTZ(0) NOT NULL,
  deleted                                     BOOLEAN        NOT NULL DEFAULT FALSE,
  deleted_date                                TIMESTAMPTZ(0)
);
