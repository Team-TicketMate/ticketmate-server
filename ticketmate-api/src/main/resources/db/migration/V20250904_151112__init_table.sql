-- Vector extension (정규화에서 제거 대상이므로 비교 무영향)
CREATE EXTENSION IF NOT EXISTS vector;

-- 생성 순서 주의: 참조 대상 테이블이 먼저 생성되도록 정렬
-- 1) 기초 테이블들 ------------------------------------------------------------

-- member
create table public.member
(
    is_first_login          boolean,
    totp_enabled            boolean      not null,
    created_date            timestamp(0) not null,
    follower_count          bigint       not null,
    following_count         bigint       not null,
    last_login_time         timestamp(0),
    updated_date            timestamp(0) not null,
    member_id               uuid         not null
        primary key,
    account_status          varchar(255)
        constraint member_account_status_check
            check ((account_status)::text = ANY ((ARRAY ['ACTIVE_ACCOUNT'::character varying, 'DELETE_ACCOUNT'::character varying])::text[])),
    birth_day               varchar(255),
    birth_year              varchar(255),
    gender                  varchar(255),
    member_type             varchar(255)
        constraint member_member_type_check
            check ((member_type)::text = ANY ((ARRAY ['AGENT'::character varying, 'CLIENT'::character varying])::text[])),
    name                    varchar(255) not null,
    nickname                varchar(255)
        unique,
    password                varchar(255),
    phone                   varchar(255),
    profile_img_stored_path varchar(255),
    role                    varchar(255)
        constraint member_role_check
            check ((role)::text = ANY ((ARRAY ['ROLE_USER'::character varying, 'ROLE_ADMIN'::character varying, 'ROLE_TEST'::character varying, 'ROLE_TEST_ADMIN'::character varying])::text[])),
    social_login_id         varchar(255),
    social_platform         varchar(255)
        constraint member_social_platform_check
            check ((social_platform)::text = ANY ((ARRAY ['NORMAL'::character varying, 'NAVER'::character varying, 'KAKAO'::character varying, 'GOOGLE'::character varying])::text[])),
    totp_secret             varchar(255),
    username                varchar(255)
        unique
);

-- concert_hall
create table public.concert_hall
(
    created_date      timestamp(0) not null,
    updated_date      timestamp(0) not null,
    concert_hall_id   uuid         not null
        primary key,
    address           varchar(255),
    city              varchar(255)
        constraint concert_hall_city_check
            check ((city)::text = ANY
                   ((ARRAY ['SEOUL'::character varying, 'BUSAN'::character varying, 'DAEGU'::character varying, 'INCHEON'::character varying, 'GWANGJU'::character varying, 'DAEJEON'::character varying, 'ULSAN'::character varying, 'SEJONG'::character varying, 'GYEONGGI'::character varying, 'GANGWON'::character varying, 'CHUNGCHEONG_BUK'::character varying, 'CHUNGCHEONG_NAM'::character varying, 'JEOLLA_BUK'::character varying, 'JEOLLA_NAM'::character varying, 'GYEONGSANG_BUK'::character varying, 'GYEONGSANG_NAM'::character varying, 'JEJU'::character varying])::text[])),
    concert_hall_name varchar(255) not null,
    web_site_url      varchar(255)
);

-- 2) concert, ticket_open_date, concert_date ----------------------------------

-- concert (FK → concert_hall)
create table public.concert
(
    created_date                  timestamp(0) not null,
    updated_date                  timestamp(0) not null,
    concert_hall_concert_hall_id  uuid
        constraint fkth8bqg3nq68wju449k245et3c
            references concert_hall,
    concert_id                    uuid         not null
        primary key,
    concert_name                  varchar(255) not null,
    concert_thumbnail_stored_path varchar(255) not null
        unique,
    concert_type                  varchar(255) not null
        constraint concert_concert_type_check
            check ((concert_type)::text = ANY ((ARRAY ['CONCERT'::character varying, 'MUSICAL'::character varying, 'SPORTS'::character varying, 'CLASSIC'::character varying, 'EXHIBITIONS'::character varying, 'OPERA'::character varying, 'ETC'::character varying])::text[])),
    seating_chart_stored_path     varchar(255)
        unique,
    ticket_reservation_site       varchar(255)
        constraint concert_ticket_reservation_site_check
            check ((ticket_reservation_site)::text = ANY ((ARRAY ['INTERPARK_TICKET'::character varying, 'YES24_TICKET'::character varying, 'TICKET_LINK'::character varying, 'MELON_TICKET'::character varying, 'COUPANG_PLAY'::character varying, 'ETC'::character varying])::text[]))
);

-- ticket_open_date (FK → concert)
create table public.ticket_open_date
(
    is_bank_transfer    boolean,
    request_max_count   integer,
    created_date        timestamp(0) not null,
    open_date           timestamp(0),
    updated_date        timestamp(0) not null,
    concert_concert_id  uuid         not null
        constraint fk4hw5il7wd2wv4tuokpxuujynl
            references concert,
    ticket_open_date_id uuid         not null
        primary key,
    ticket_open_type    varchar(255) not null
        constraint ticket_open_date_ticket_open_type_check
            check ((ticket_open_type)::text = ANY ((ARRAY ['GENERAL_OPEN'::character varying, 'PRE_OPEN'::character varying])::text[]))
);

-- concert_date (FK → concert)
create table public.concert_date
(
    session            integer      not null,
    created_date       timestamp(0) not null,
    performance_date   timestamp(0) not null,
    updated_date       timestamp(0) not null,
    concert_concert_id uuid         not null
        constraint fk80mwenkwnbfy4qtdvpu6cuj0e
            references concert,
    concert_date_id    uuid         not null
        primary key
);

-- 3) application_form, detail, hope_area, rejection_reason --------------------

-- application_form (FK → member, concert, ticket_open_date)
create table public.application_form
(
    created_date                         timestamp(0) not null,
    updated_date                         timestamp(0) not null,
    agent_member_id                      uuid         not null
        constraint fkr4ois7qc6ctdll0he1l41obcc
            references member,
    application_form_id                  uuid         not null
        primary key,
    client_member_id                     uuid         not null
        constraint fkkv615oe2l2d5avk4xedql4fij
            references member,
    concert_concert_id                   uuid         not null
        constraint fk5p7n9psh1a5nqmvo6855esv95
            references concert,
    ticket_open_date_ticket_open_date_id uuid         not null
        constraint fkkwb0um4kn7sf5gfsnetv5e7ja
            references ticket_open_date,
    application_form_status              varchar(255) not null
        constraint application_form_application_form_status_check
            check ((application_form_status)::text = ANY ((ARRAY ['PENDING'::character varying, 'APPROVED'::character varying, 'CANCELED'::character varying, 'REJECTED'::character varying, 'CANCELED_IN_PROCESS'::character varying])::text[])),
    ticket_open_type                     varchar(255) not null
        constraint application_form_ticket_open_type_check
            check ((ticket_open_type)::text = ANY ((ARRAY ['GENERAL_OPEN'::character varying, 'PRE_OPEN'::character varying])::text[]))
);

-- application_form_detail (FK → application_form, concert_date)
create table public.application_form_detail
(
    request_count                        integer      not null,
    created_date                         timestamp(0) not null,
    updated_date                         timestamp(0) not null,
    application_form_application_form_id uuid         not null
        constraint fkketw9d39thw07246ax2r9qcn
            references application_form,
    application_form_detail_id           uuid         not null
        primary key,
    concert_date_concert_date_id         uuid         not null
        constraint fkg9spgxuwgt0f5dw5g9shjyxx6
            references concert_date,
    requirement                          varchar(100)
);

-- hope_area (FK → application_form_detail, priority 범위 체크 포함)
create table public.hope_area
(
    price                                              integer      not null,
    priority                                           integer      not null
        constraint hope_area_priority_check
            check ((priority >= 1) AND (priority <= 10)),
    created_date                                       timestamp(0) not null,
    updated_date                                       timestamp(0) not null,
    application_form_detail_application_form_detail_id uuid         not null
        constraint fk353adjv2paub8tpmb7a2spoq2
            references application_form_detail,
    hope_area_id                                       uuid         not null
        primary key,
    location                                           varchar(255) not null
);

-- rejection_reason (FK → application_form, unique on application_form_application_form_id)
create table public.rejection_reason
(
    created_date                         timestamp(0) not null,
    updated_date                         timestamp(0) not null,
    application_form_application_form_id uuid
        unique
        constraint fk1hd2nxxt0heaqfert1qcpsffw
            references application_form,
    rejection_reason_id                  uuid         not null
        primary key,
    application_form_rejected_type       varchar(255) not null
        constraint rejection_reason_application_form_rejected_type_check
            check ((application_form_rejected_type)::text = ANY ((ARRAY ['FEE_NOT_MATCHING_MARKET_PRICE'::character varying, 'RESERVATION_CLOSED'::character varying, 'SCHEDULE_UNAVAILABLE'::character varying, 'OTHER'::character varying])::text[])),
    other_memo                           varchar(255)
);

-- 4) 나머지 테이블들 ----------------------------------------------------------

-- concert_agent_availability (FK → member, concert)
create table public.concert_agent_availability
(
    accepting               boolean      not null,
    created_date            timestamp(0) not null,
    updated_date            timestamp(0) not null,
    agent_member_id         uuid         not null
        constraint fkqr8kb7wi08v8jh23oeiyfq7tr
            references member,
    concert_agent_option_id uuid         not null
        primary key,
    concert_concert_id      uuid         not null
        constraint fk5tt7s2tag1x0jhj7vikx3h5g8
            references concert,
    introduction            varchar(255)
);

-- member_follow (FK → member, member)
create table public.member_follow
(
    created_date       timestamp(0) not null,
    updated_date       timestamp(0) not null,
    followee_member_id uuid         not null
        constraint fkyiiutd5sxyedjd214ucbo6r2
            references member,
    follower_member_id uuid         not null
        constraint fk5wdmwwe0ta2mk1fgj0c29q1ep
            references member,
    member_follow_id   uuid         not null
        primary key
);

-- portfolio (FK → member, member_member_id unique)
create table public.portfolio
(
    created_date          timestamp(0) not null,
    updated_date          timestamp(0) not null,
    member_member_id      uuid         not null
        unique
        constraint fkmvpq39amjclk8wrsajap58kf3
            references member,
    portfolio_id          uuid         not null
        primary key,
    portfolio_description varchar(255) not null,
    portfolio_status      varchar(255) not null
        constraint portfolio_portfolio_status_check
            check ((portfolio_status)::text = ANY ((ARRAY ['PENDING_REVIEW'::character varying, 'REVIEWING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))
);

-- portfolio_img (FK → portfolio)
create table public.portfolio_img
(
    created_date           timestamp(0) not null,
    size_bytes             bigint       not null,
    updated_date           timestamp(0) not null,
    portfolio_img_id       uuid         not null
        primary key,
    portfolio_portfolio_id uuid         not null
        constraint fk6ffef8swvh23bwnfbyrxc8vu7
            references portfolio,
    stored_path            varchar(512) not null
        unique,
    file_extension         varchar(255) not null
        constraint portfolio_img_file_extension_check
            check ((file_extension)::text = ANY ((ARRAY ['JPG'::character varying, 'JPEG'::character varying, 'PNG'::character varying, 'GIF'::character varying, 'BMP'::character varying, 'WEBP'::character varying])::text[])),
    original_filename      varchar(255) not null
);

-- embedding (text unique)
create table public.embedding
(
    created_date     timestamp(0) not null,
    updated_date     timestamp(0) not null,
    embedding_id     uuid         not null
        primary key,
    target_id        uuid,
    embedding_type   varchar(255) not null
        constraint embedding_embedding_type_check
            check ((embedding_type)::text = ANY ((ARRAY ['CONCERT'::character varying, 'AGENT'::character varying, 'SEARCH'::character varying])::text[])),
    text             varchar(255) not null
        unique,
    embedding_vector vector(768)  not null
);

-- agent_performance_summary (FK → member)
create table public.agent_performance_summary
(
    average_rating       double precision not null,
    recent_success_count integer          not null,
    review_count         integer          not null,
    total_score          double precision not null,
    created_date         timestamp(0)     not null,
    updated_date         timestamp(0)     not null,
    agent_member_id      uuid             not null
        primary key
        constraint fkso0nncg77tlryqteu32w6ibhp
            references member
);
