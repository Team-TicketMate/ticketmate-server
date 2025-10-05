CREATE TABLE public.review
(
    review_id                               UUID           NOT NULL PRIMARY KEY,
    application_form_application_form_id    UUID           NOT NULL UNIQUE REFERENCES application_form,
    client_member_id                        UUID           NOT NULL REFERENCES member,
    agent_member_id                         UUID           NOT NULL REFERENCES member,
    rating                                  REAL           NOT NULL,
    comment                                 VARCHAR(300)   NOT NULL,
    agent_comment                           VARCHAR(300)   NULL,
    agent_commented_date                    TIMESTAMPTZ(0) NULL,
    created_date                            TIMESTAMPTZ(0) NOT NULL,
    updated_date                            TIMESTAMPTZ(0) NOT NULL
);

CREATE TABLE public.review_img
(
    review_img_id                           UUID           NOT NULL PRIMARY KEY,
    review_review_id                        UUID           NOT NULL REFERENCES review,
    original_filename                       VARCHAR(255)   NOT NULL,
    stored_path                             VARCHAR(255)   NOT NULL UNIQUE,
    file_extension                          VARCHAR(255)   NOT NULL
        constraint review_img_file_extension_check
            check ((file_extension)::text = ANY ((ARRAY ['JPG'::character varying, 'JPEG'::character varying, 'PNG'::character varying, 'GIF'::character varying, 'BMP'::character varying, 'WEBP'::character varying])::text[])),
    size_bytes                              BIGINT         NOT NULL,
    created_date                            TIMESTAMPTZ(0) NOT NULL,
    updated_date                            TIMESTAMPTZ(0) NOT NULL
);

-- agent_performance_summary 테이블에 total_rating_sum 컬럼 추가 (마이그레이션용 기본값 포함)
ALTER TABLE public.agent_performance_summary
    ADD COLUMN total_rating_sum DOUBLE PRECISION NOT NULL DEFAULT 0;

-- default 제거
ALTER TABLE public.agent_performance_summary
    ALTER COLUMN total_rating_sum DROP DEFAULT;