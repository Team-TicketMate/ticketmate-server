CREATE TABLE public.review
(
    review_id           UUID           NOT NULL PRIMARY KEY,
    application_form_id UUID           NOT NULL UNIQUE
        CONSTRAINT fk_review_on_application_form REFERENCES application_form,
    client_id           UUID           NOT NULL
        CONSTRAINT fk_review_on_client_member REFERENCES member,
    agent_id            UUID           NOT NULL
        CONSTRAINT fk_review_on_agent_member REFERENCES member,
    rating              REAL           NOT NULL,
    comment             VARCHAR(300)   NOT NULL,
    created_date        TIMESTAMPTZ(0) NOT NULL,
    updated_date        TIMESTAMPTZ(0) NOT NULL
);

CREATE TABLE public.review_img
(
    review_img_id UUID           NOT NULL PRIMARY KEY,
    review_id     UUID           NOT NULL
        CONSTRAINT fk_review_img_on_review REFERENCES review,
    file_url      VARCHAR(255)   NOT NULL,
    stored_path   VARCHAR(255)   NOT NULL UNIQUE,
    created_date  TIMESTAMPTZ(0) NOT NULL,
    updated_date  TIMESTAMPTZ(0) NOT NULL
);
