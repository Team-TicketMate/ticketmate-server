CREATE TABLE public.success_history(
  success_history_id                   UUID         NOT NULL PRIMARY KEY,
  fulfillment_form_fulfillment_form_id UUID         NOT NULL REFERENCES fulfillment_form
    CONSTRAINT uk_success_history_fulfillment_form UNIQUE,

  success_history_status               VARCHAR(255) NOT NULL
    CONSTRAINT success_history_status_check
      CHECK (success_history_status IN (
                                        'NOT_REVIEWED',
                                        'REVIEWED'
        )),

  created_date                         TIMESTAMPTZ(0) NOT NULL,
  updated_date                         TIMESTAMPTZ(0) NOT NULL,
  deleted                              BOOLEAN      NOT NULL DEFAULT FALSE,
  deleted_date                         TIMESTAMPTZ(0)
);