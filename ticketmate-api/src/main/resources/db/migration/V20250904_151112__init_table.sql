CREATE EXTENSION IF NOT EXISTS vector;

-- 1) TABLES (FK 없이 생성)
CREATE TABLE public.member (
                               is_first_login boolean,
                               totp_enabled boolean NOT NULL,
                               created_date timestamp(0) without time zone NOT NULL,
                               follower_count bigint NOT NULL,
                               following_count bigint NOT NULL,
                               last_login_time timestamp(0) without time zone,
                               updated_date timestamp(0) without time zone NOT NULL,
                               member_id uuid PRIMARY KEY NOT NULL,
                               account_status character varying(255),
                               birth_day character varying(255),
                               birth_year character varying(255),
                               gender character varying(255),
                               member_type character varying(255),
                               name character varying(255) NOT NULL,
                               nickname character varying(255),
                               password character varying(255),
                               phone character varying(255),
                               profile_img_stored_path character varying(255),
                               role character varying(255),
                               social_login_id character varying(255),
                               social_platform character varying(255),
                               totp_secret character varying(255),
                               username character varying(255)
);

CREATE TABLE public.concert_hall (
                                     created_date timestamp(0) without time zone NOT NULL,
                                     updated_date timestamp(0) without time zone NOT NULL,
                                     concert_hall_id uuid PRIMARY KEY NOT NULL,
                                     address character varying(255),
                                     city character varying(255),
                                     concert_hall_name character varying(255) NOT NULL,
                                     web_site_url character varying(255)
);

CREATE TABLE public.concert (
                                created_date timestamp(0) without time zone NOT NULL,
                                updated_date timestamp(0) without time zone NOT NULL,
                                concert_hall_concert_hall_id uuid,
                                concert_id uuid PRIMARY KEY NOT NULL,
                                concert_name character varying(255) NOT NULL,
                                concert_thumbnail_stored_path character varying(255) NOT NULL,
                                concert_type character varying(255) NOT NULL,
                                seating_chart_stored_path character varying(255),
                                ticket_reservation_site character varying(255)
);

CREATE TABLE public.concert_date (
                                     session integer NOT NULL,
                                     created_date timestamp(0) without time zone NOT NULL,
                                     performance_date timestamp(0) without time zone NOT NULL,
                                     updated_date timestamp(0) without time zone NOT NULL,
                                     concert_concert_id uuid NOT NULL,
                                     concert_date_id uuid PRIMARY KEY NOT NULL
);

CREATE TABLE public.ticket_open_date (
                                         is_bank_transfer boolean,
                                         request_max_count integer,
                                         created_date timestamp(0) without time zone NOT NULL,
                                         open_date timestamp(0) without time zone,
                                         updated_date timestamp(0) without time zone NOT NULL,
                                         concert_concert_id uuid NOT NULL,
                                         ticket_open_date_id uuid PRIMARY KEY NOT NULL,
                                         ticket_open_type character varying(255) NOT NULL
);

CREATE TABLE public.application_form (
                                         created_date timestamp(0) without time zone NOT NULL,
                                         updated_date timestamp(0) without time zone NOT NULL,
                                         agent_member_id uuid NOT NULL,
                                         application_form_id uuid PRIMARY KEY NOT NULL,
                                         client_member_id uuid NOT NULL,
                                         concert_concert_id uuid NOT NULL,
                                         ticket_open_date_ticket_open_date_id uuid NOT NULL,
                                         application_form_status character varying(255) NOT NULL,
                                         ticket_open_type character varying(255) NOT NULL
);

CREATE TABLE public.application_form_detail (
                                                request_count integer NOT NULL,
                                                created_date timestamp(0) without time zone NOT NULL,
                                                updated_date timestamp(0) without time zone NOT NULL,
                                                application_form_application_form_id uuid NOT NULL,
                                                application_form_detail_id uuid PRIMARY KEY NOT NULL,
                                                concert_date_concert_date_id uuid NOT NULL,
                                                requirement character varying(100)
);

CREATE TABLE public.hope_area (
                                  price integer NOT NULL,
                                  priority integer NOT NULL,
                                  created_date timestamp(0) without time zone NOT NULL,
                                  updated_date timestamp(0) without time zone NOT NULL,
                                  application_form_detail_application_form_detail_id uuid NOT NULL,
                                  hope_area_id uuid PRIMARY KEY NOT NULL,
                                  location character varying(255) NOT NULL
);

CREATE TABLE public.concert_agent_availability (
                                                   accepting boolean NOT NULL,
                                                   created_date timestamp(0) without time zone NOT NULL,
                                                   updated_date timestamp(0) without time zone NOT NULL,
                                                   agent_member_id uuid NOT NULL,
                                                   concert_agent_option_id uuid PRIMARY KEY NOT NULL,
                                                   concert_concert_id uuid NOT NULL,
                                                   introduction character varying(255)
);

CREATE TABLE public.member_follow (
                                      created_date timestamp(0) without time zone NOT NULL,
                                      updated_date timestamp(0) without time zone NOT NULL,
                                      followee_member_id uuid NOT NULL,
                                      follower_member_id uuid NOT NULL,
                                      member_follow_id uuid PRIMARY KEY NOT NULL
);

CREATE TABLE public.portfolio (
                                  created_date timestamp(0) without time zone NOT NULL,
                                  updated_date timestamp(0) without time zone NOT NULL,
                                  member_member_id uuid NOT NULL,
                                  portfolio_id uuid PRIMARY KEY NOT NULL,
                                  portfolio_description character varying(255) NOT NULL,
                                  portfolio_status character varying(255) NOT NULL
);

CREATE TABLE public.portfolio_img (
                                      created_date timestamp(0) without time zone NOT NULL,
                                      size_bytes bigint NOT NULL,
                                      updated_date timestamp(0) without time zone NOT NULL,
                                      portfolio_img_id uuid PRIMARY KEY NOT NULL,
                                      portfolio_portfolio_id uuid NOT NULL,
                                      stored_path character varying(512) NOT NULL,
                                      file_extension character varying(255) NOT NULL,
                                      original_filename character varying(255) NOT NULL
);

CREATE TABLE public.rejection_reason (
                                         created_date timestamp(0) without time zone NOT NULL,
                                         updated_date timestamp(0) without time zone NOT NULL,
                                         application_form_application_form_id uuid,
                                         rejection_reason_id uuid PRIMARY KEY NOT NULL,
                                         application_form_rejected_type character varying(255) NOT NULL,
                                         other_memo character varying(255)
);

CREATE TABLE public.agent_performance_summary (
                                                  average_rating double precision NOT NULL,
                                                  recent_success_count integer NOT NULL,
                                                  review_count integer NOT NULL,
                                                  total_score double precision NOT NULL,
                                                  created_date timestamp(0) without time zone NOT NULL,
                                                  updated_date timestamp(0) without time zone NOT NULL,
                                                  agent_member_id uuid PRIMARY KEY NOT NULL
);

CREATE TABLE public.embedding (
                                  created_date timestamp(0) without time zone NOT NULL,
                                  updated_date timestamp(0) without time zone NOT NULL,
                                  embedding_id uuid PRIMARY KEY NOT NULL,
                                  target_id uuid,
                                  embedding_type character varying(255) NOT NULL,
                                  text character varying(255) NOT NULL,
                                  embedding_vector vector(768) NOT NULL
);

-- 2) UNIQUE INDEXES (Hibernate가 만든 이름 그대로)
CREATE UNIQUE INDEX concert_concert_thumbnail_stored_path_key ON public.concert USING btree (concert_thumbnail_stored_path);
CREATE UNIQUE INDEX concert_seating_chart_stored_path_key ON public.concert USING btree (seating_chart_stored_path);
CREATE UNIQUE INDEX embedding_text_key ON public.embedding USING btree (text);
CREATE UNIQUE INDEX member_nickname_key ON public.member USING btree (nickname);
CREATE UNIQUE INDEX member_username_key ON public.member USING btree (username);
CREATE UNIQUE INDEX portfolio_member_member_id_key ON public.portfolio USING btree (member_member_id);
CREATE UNIQUE INDEX portfolio_img_stored_path_key ON public.portfolio_img USING btree (stored_path);
CREATE UNIQUE INDEX rejection_reason_application_form_application_form_id_key ON public.rejection_reason USING btree (application_form_application_form_id);

-- 3) FOREIGN KEYS (순서 안전성을 위해 ALTER로 추가)
ALTER TABLE public.concert
    ADD FOREIGN KEY (concert_hall_concert_hall_id) REFERENCES public.concert_hall (concert_hall_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.concert_date
    ADD FOREIGN KEY (concert_concert_id) REFERENCES public.concert (concert_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.ticket_open_date
    ADD FOREIGN KEY (concert_concert_id) REFERENCES public.concert (concert_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.application_form
    ADD FOREIGN KEY (concert_concert_id) REFERENCES public.concert (concert_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.application_form
    ADD FOREIGN KEY (client_member_id) REFERENCES public.member (member_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.application_form
    ADD FOREIGN KEY (ticket_open_date_ticket_open_date_id) REFERENCES public.ticket_open_date (ticket_open_date_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.application_form
    ADD FOREIGN KEY (agent_member_id) REFERENCES public.member (member_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.application_form_detail
    ADD FOREIGN KEY (concert_date_concert_date_id) REFERENCES public.concert_date (concert_date_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.application_form_detail
    ADD FOREIGN KEY (application_form_application_form_id) REFERENCES public.application_form (application_form_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.hope_area
    ADD FOREIGN KEY (application_form_detail_application_form_detail_id) REFERENCES public.application_form_detail (application_form_detail_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.concert_agent_availability
    ADD FOREIGN KEY (concert_concert_id) REFERENCES public.concert (concert_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.concert_agent_availability
    ADD FOREIGN KEY (agent_member_id) REFERENCES public.member (member_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.member_follow
    ADD FOREIGN KEY (follower_member_id) REFERENCES public.member (member_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.member_follow
    ADD FOREIGN KEY (followee_member_id) REFERENCES public.member (member_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.portfolio
    ADD FOREIGN KEY (member_member_id) REFERENCES public.member (member_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.portfolio_img
    ADD FOREIGN KEY (portfolio_portfolio_id) REFERENCES public.portfolio (portfolio_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.rejection_reason
    ADD FOREIGN KEY (application_form_application_form_id) REFERENCES public.application_form (application_form_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.agent_performance_summary
    ADD FOREIGN KEY (agent_member_id) REFERENCES public.member (member_id)
        MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
